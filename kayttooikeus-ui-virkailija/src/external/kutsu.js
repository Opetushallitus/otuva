import Bacon from "baconjs";
import {urlsP} from "./urls";
import dispatcher from "../logic/dispatcher";
import {handleFetchError} from "../logic/fetchUtils";
import {commonHandleError} from "../logic/error";

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
    const response = Bacon.fromPromise(fetch(window.url('kayttooikeus-service.peruutaKutsu', id),
        {method: 'DELETE', credentials: 'same-origin'})
        .then(handleFetchError).then(response => {
            kutsuList.update();
            return response;
        }));
    response.onError(commonHandleError);
    return response;
};

export const kutsuListStateP = kutsuList.toProperty();
export const kutsuListStateS = Bacon.combineWith(kutsuListStateP, urlsP, (state, urls) => ({state, urls})).changes()
    .skipWhile(({state}) => !state.active);
export const kutsuListResponseS = kutsuListStateS.flatMap(({state}) => 
    Bacon.fromPromise(fetch(window.url('kayttooikeus-service.kutsu', state.params),{credentials: 'same-origin'})
        .then(handleFetchError).then(response => response.json().then(json => ({loaded: true, result: json}))
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
        }).then(handleFetchError).then(response => {
            kutsuList.update();
            return response.text().then(txt => parseInt(txt, 10));
        })));
    invitationResponseS.onError(commonHandleError);
    const invitationResponsePendingP = invitationRequestS
        .awaiting(invitationResponseS);
    return {
        invitationResponseS,
        invitationResponsePendingP
    }
}