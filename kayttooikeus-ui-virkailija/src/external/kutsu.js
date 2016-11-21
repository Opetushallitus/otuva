import Bacon from "baconjs";
import {urlsP} from "./urls";
import dispatcher from "../logic/dispatcher";
import {queryString, handleError} from "../logic/fetchUtils";

const kutsuListDispatcher = dispatcher();

const kutsuList = {
    toProperty: (initialState = {
        params: {
            sortBy: 'AIKALEIMA',
            direction: 'DESC'
        }
    }) => Bacon.update(initialState,
        [kutsuListDispatcher.stream('update')], (current) => ({...current}),
        [kutsuListDispatcher.stream('activate')], (current) => current.active ? current : ({...current, active:true}),
        [kutsuListDispatcher.stream('changeParams')], (current, params) => ({...current, params})
    ),
    order: (by, direction='ASC') => kutsuListDispatcher.push('changeParams', {
        sortBy: by,
        direction: direction
    }),
    update: () => kutsuListDispatcher.push('update'),
    activate: () => kutsuListDispatcher.push('activate')
};

export const peruutaKutsu = (id) => {
    return Bacon.fromPromise(fetch(window.url('kayttooikeus-service.peruutaKutsu', id), {
            method: 'DELETE'
    }).then(handleError).then(response => {
        kutsuList.update();
        return response;
    }));
};

export const kutsuListStateP = kutsuList.toProperty();
export const kutsuListStateS = Bacon.combineWith(kutsuListStateP, urlsP, (state, urls) => ({state, urls})).changes()
    .skipWhile(({state}) => !state.active);
export const kutsuListResponseS = kutsuListStateS.flatMap(({state}) => 
    Bacon.fromPromise(fetch(window.url('kayttooikeus-service.kutsu')+queryString(state.params))
        .then(handleError).then(response => response.json().then(json => ({loaded: true, result: json}))
            .catch(() => ({loaded:false, result:[]}))))).toEventStream();
export const kutsuListP = kutsuListResponseS.toProperty({loaded:false, result:[]});
export default kutsuList

export function kutsu(payload) {
    const invitationRequestS = Bacon.later(0, {
        url: window.url('kayttooikeus-service.kutsu'), payload
    });
    const invitationResponseS = invitationRequestS.flatMap(({url, payload}) => Bacon.fromPromise(
        fetch(url, {
            method: 'POST',
            body: JSON.stringify(payload),
            headers: {'Content-Type': 'application/json'},
            credentials: 'same-origin'
        }).then(handleError).then(response => {
            kutsuList.update();
            return response.text().then(txt => parseInt(txt, 10));
        })));
    const invitationResponsePendingP = invitationRequestS
        .awaiting(invitationResponseS);
    return {
        invitationResponseS,
        invitationResponsePendingP
    }
}