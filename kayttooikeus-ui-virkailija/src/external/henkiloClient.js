import Bacon from "baconjs";
import {handleFetchError} from "../logic/fetchUtils";
import {locationP} from "../logic/location";
import {commonHandleError} from "../logic/error";
import dispatcher from "../logic/dispatcher";
import {organisationByOid} from "./organisaatioClient";

export const henkiloBus = new Bacon.Bus();

henkiloBus.log('test');

const henkiloBusS = henkiloBus.toEventStream().log('tst');

export const henkiloP = Bacon.update({},
    [henkiloBusS], (prev, oid) => {
        console.log('update');
        return oid;
        // return findByOid(oid);
    }
);
const x = henkiloP.onValue();

locationP.flatMap(location => {
    console.log('lok2');
    const oid = location.params['oid'];
    findByOid(oid).onValue(value => {
        console.log(value);
        return henkiloBus.push(value);
    });
    // henkiloBus.push(oid);
}).onValue();

locationP.log('lok');

export const updateHenkilo = payload => {
    const response = Bacon.fromPromise(fetch(window.url('oppijanumerorekisteri-service.henkilo'),
        {
            method: 'PUT',
            credentials: 'same-origin',
            body: JSON.stringify(payload),
            headers: {'Content-Type': 'application/json'},
        })
        .then(handleFetchError).then(response => {
            henkiloBus.push(payload.oid);
            return response;
        }));
    response.onError(commonHandleError);
    return response;
};

const findByOid = (oid) => {
    const response = Bacon.fromPromise(fetch(window.url('oppijanumerorekisteri-service.henkilo.oid', oid), {credentials: 'same-origin'})
        .then(handleFetchError)
        .then(response => {
            console.log(response);
            return response.json().then(json => ({loaded: true, result: json}))
        })
        .catch(e => console.error(e)));
    response.onError(commonHandleError);
    return response;
};




// export const henkiloP = Bacon.combineWith(locationP, (location) => {
//     const oid = location.params['oid'];
//     const henkiloS = Bacon.fromPromise(fetch(window.url('oppijanumerorekisteri-service.henkilo.oid', oid), {credentials: 'same-origin'})
//         .then(handleFetchError)
//         .then(response => {
//             console.log(response);
//             return response.json().then(json => ({loaded: true, result: json}))
//         })
//         .catch(e => console.error(e)));
//     return henkiloS;
// }).flatMap(r => r);

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

