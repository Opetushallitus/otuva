import Bacon from "baconjs";
import {locationP} from "../logic/location";
import {organisationByOid} from "./organisaatioClient";
import http from "../external/http"

export const henkiloBus = new Bacon.Bus();

// Property containing henkilo info. Returns new event if henkilo info changes.
export const henkiloP = Bacon.update({},
    [henkiloBus], (prev, oid) => {
        return oid;
    }
);
// onValue() so stream stays alive.
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

export const henkiloOrganisationsP = henkiloOrganisationsS.flatMap(value => {
    const organisationInfoList = value.map(organisaatioHenkilo => {
        return organisationByOid(organisaatioHenkilo.organisaatioOid);
    });
    return Bacon.zipWith(organisationInfoList, function(...results) {
        // include organisation henkilo to the result
        return results.map(organisation => {
            organisation.orgHenkilo = value.filter(orgHenkilo => {
                return orgHenkilo.organisaatioOid === organisation.oid;
            })[0];
            return organisation;
        });
    });
}).toProperty();

export const kayttajatietoP = locationP.flatMap(location => {
    const oid = location.params['oid'];
    return http.get(window.url('kayttooikeus-service.henkilo.kayttajatieto', oid));
}).toProperty();

