import Bacon from "baconjs";
import {handleFetchError} from "../logic/fetchUtils";
import {locationP} from "../logic/location";
import {commonHandleError} from "../logic/error";
import dispatcher from "../logic/dispatcher";
import {organisationByOid} from "./organisaatioClient";

const henkiloDispatcher = dispatcher();

const henkilo = {
    toProperty: (initialState = {
        params: {
        }
    }) => Bacon.update(initialState,
        [henkiloDispatcher.stream('update')], (current) => ({...current}),
    ),
    update: () => henkiloDispatcher.push('update'),
};

export const updateHenkilo = payload => {
    const response = Bacon.fromPromise(fetch(window.url('oppijanumerorekisteri-service.henkilo'),
        {
            method: 'PUT',
            credentials: 'same-origin',
            body: JSON.stringify(payload),
            headers: {'Content-Type': 'application/json'},
        })
        .then(handleFetchError).then(response => {
            henkilo.update();
            return response;
        }));
    response.onError(commonHandleError);
    return response;
};

export const henkiloP = Bacon.combineWith(locationP, (location) => {
    const oid = location.params['oid'];
    return Bacon.fromPromise(fetch(window.url('oppijanumerorekisteri-service.henkilo.oid', oid), {credentials: 'same-origin'})
        .then(handleFetchError)
        .then(response => response.json().then(json => ({loaded: true, result: json})))
        .catch(e => console.error(e)));
}).flatMap(r => r);

export const henkiloOrganisationsS = Bacon.combineWith(locationP, (location) => {
    const oid = location.params['oid'];
    return Bacon.fromPromise(fetch(window.url('kayttooikeus-service.henkilo.organisaatiohenkilos', oid), {credentials: 'same-origin'})
        .then(handleFetchError)
        .then(response => response.json().then(json => ({loaded: true, result: json})))
        .catch(e => console.error(e)));
}).flatMap(r => r);

export const henkiloOrganisationsP = henkiloOrganisationsS.filter('.loaded').map(value => {
    const organisationInfoList = value.result.map(organisaatioHenkilo => {
        return organisationByOid(organisaatioHenkilo.organisaatioOid);
    });
    return Bacon.zipWith(organisationInfoList, function(...results) {
        // include organisation henkilo to the result
        return results.map(organisation => {
            organisation.result.orgHenkilo = value.result.filter(orgHenkilo => {
                return orgHenkilo.organisaatioOid === organisation.result.oid;
            })[0];
            return organisation;
        });
    });
}).flatMap(r => r);

export const kayttajatietoP = Bacon.combineWith(locationP, (location) => {
    const oid = location.params['oid'];
    return Bacon.fromPromise(fetch(window.url('kayttooikeus-service.henkilo.kayttajatieto', oid), {credentials: 'same-origin'})
        .then(handleFetchError)
        .then(response => response.json().then(json => ({loaded: true, result: json})))
        .catch(e => console.error(e)));
}).flatMap(r => r);

