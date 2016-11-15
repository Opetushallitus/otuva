import Bacon from 'baconjs'
import dispatcher from '../logic/dispatcher'
import {queryString} from '../logic/fetchUtils'

const kutsuListDispatcher = dispatcher();

const kutsuList = {
    toProperty: (initialState = {
        params: {
            sortBy: 'AIKALEIMA',
            direction: 'DESC'
        }
    }) => Bacon.update(initialState,
        [kutsuListDispatcher.stream('update')], (current) => ({...current}),
        [kutsuListDispatcher.stream('changeParams')], (current, params) => ({...current, params})
    ),
    order: (by, direction='ASC') => kutsuListDispatcher.push('changeParams', {
        sortBy: by,
        direction: direction
    }),
    update: () => kutsuListDispatcher.push('update')
};

export const peruutaKutsu = (id) => {
    return Bacon.fromPromise(fetch(window.url('kayttooikeus-service.peruutaKutsu', id), {
        method: 'DELETE'
    }).then(response => {
        kutsuList.update();
        return response;
    }));
};

export const kutsuListStateP = kutsuList.toProperty();
export const kutsuListRequestS = Bacon.combineTemplate({state: kutsuListStateP}).changes();
export const kutsuListResponseS = kutsuListRequestS.flatMap(stateHolder => 
    Bacon.fromPromise(fetch(window.url('kayttooikeus-service.kutsu')+queryString(stateHolder.state.params))
        .then(response => response.json().then(json => ({loaded: true, result: json}))
            .catch(no => ({loaded:false, result:null}))))
    );
export const kutsuListP = kutsuListResponseS.toProperty({loaded:false, result:[]});
export default kutsuList
