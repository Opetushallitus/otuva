import Bacon from "baconjs";
import {handleFetchError} from "../logic/fetchUtils";
import {locationP} from "../logic/location";
import {commonHandleError} from "../logic/error";
import dispatcher from "../logic/dispatcher";

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
}).flatMap(result => result).toEventStream().toProperty({loaded: false, result: {}});

export const henkiloOrganisationsP = Bacon.combineWith(locationP, (location) => {
    const oid = location.params['oid'];
    return Bacon.fromPromise(fetch(window.url('kayttooikeus-service.henkilo.organisaatiohenkilos', oid), {credentials: 'same-origin'})
        .then(handleFetchError)
        .then(response => response.json().then(json => ({loaded: true, result: json})))
        .catch(e => console.error(e)));
}).flatMap(result => result).toEventStream().toProperty({loaded: false, result: {}});

