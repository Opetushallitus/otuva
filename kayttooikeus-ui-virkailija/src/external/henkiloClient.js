import Bacon from "baconjs";
import {handleFetchError} from "../logic/fetchUtils";
import {locationP} from "../logic/location";
import {organisationByOid} from "./organisaatioClient";
import http from "../external/http"

export const henkiloBus = new Bacon.Bus();

export const henkiloP = Bacon.update({},
    [henkiloBus], (prev, oid) => {
        return oid;
    }
);
henkiloP.onValue();

locationP.flatMap(location => {
    const oid = location.params['oid'];
    http.get(window.url('oppijanumerorekisteri-service.henkilo.oid', oid)).onValue(value => henkiloBus.push(value));
}).onValue();

export const updateHenkilo = henkilo => {
    return http.put(window.url('oppijanumerorekisteri-service.henkilo'), henkilo);
};

export const henkiloOrganisationsS = locationP.flatMap(location => {
    const oid = location.params['oid'];
    return http.get(window.url('kayttooikeus-service.henkilo.organisaatiohenkilos', oid));
}).toProperty();

export const henkiloOrganisationsP = henkiloOrganisationsS.map(value => {
    const organisationInfoList = value.map(organisaatioHenkilo => {
        return organisationByOid(organisaatioHenkilo.organisaatioOid);
    });
    return Bacon.zipWith(organisationInfoList, function(...results) {
        // include organisation henkilo to the result
        return results.map(organisation => {
            organisation.result.orgHenkilo = value.filter(orgHenkilo => {
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

