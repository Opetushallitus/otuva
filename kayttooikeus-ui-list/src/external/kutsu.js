import Bacon from 'baconjs'
import dispatcher from '../logic/dispatcher'
import {queryString} from '../logic/fetchUtils'

const KUTSU_URL = '/kayttooikeus-service/kutsu';
const kutsuListDispatcher = dispatcher('kutsuList');

const kutsuList = {
    toProperty: (initialState = {
        active: false,
        params: {
            sortBy: 'AIKALEIMA',
            direction: 'DESC'
        }
    }) => Bacon.update(initialState,
        [kutsuListDispatcher.stream('activate')], (current) => ({...current, active: true}),
        [kutsuListDispatcher.stream('update')], (current) => ({...current}),
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
    return Bacon.fromPromise(fetch(KUTSU_URL+'/'+id, {
        method: 'DELETE'
    }).then(response => {
        kutsuList.update();
        return response;
    }));
};

export const kutsuListP = kutsuList.toProperty();
export const kutsuListRequestS = Bacon.combineTemplate({state: kutsuListP}).changes();
export const kutsuListResponseS = kutsuListRequestS.skipWhile(stateHolder => !stateHolder.state.active)
    .flatMap(stateHolder => Bacon.fromPromise(fetch(KUTSU_URL+queryString(stateHolder.state.params))
        .then(response => response.json().then(json => ({loaded: true, result: json}))
            .catch(no => ({loaded:false, result:null}))))
    );
export default kutsuList
