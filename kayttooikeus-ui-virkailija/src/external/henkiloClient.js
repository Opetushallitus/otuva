import Bacon from "baconjs";
import {handleFetchError} from "../logic/fetchUtils";
import {locationP} from "../logic/location";


export const henkiloP = Bacon.combineWith(locationP, (location) => {
    const oid = location.params['oid'];
    return Bacon.fromPromise(fetch(window.url('oppijanumerorekisteri-service.henkilo.oid', oid), {credentials: 'same-origin'})
        .then(handleFetchError)
        .then(response => response.json().then(json => ({loaded: true, result: json})))
        .catch(e => console.error(e)));
}).flatMap(result => result).toEventStream().toProperty({loaded: false, result: {}});

export const henkiloOrganisationsP = Bacon.combineWith(locationP, (location) => {
    const oid = location.params['oid'];
    return Bacon.fromPromise(fetch(window.url('kayttooikeus-service.henkilo.organisaatios', oid), {credentials: 'same-origin'})
        .then(handleFetchError)
        .then(response => response.json().then(json => ({loaded: true, result: json})))
        .catch(e => console.error(e)));
}).flatMap(result => result).toEventStream().toProperty({loaded: false, result: {}});
