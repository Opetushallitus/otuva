import Bacon from 'baconjs'
import dispatcher from '../logic/dispatcher'
import {queryString} from '../logic/fetchUtils'

const KUTSU_URL = '/kayttooikeus-service/kutsu';
const d = dispatcher();

const kutsuList = {
    toProperty: (initialState = {
        active: false,
        params: {
            sortBy: 'AIKALEIMA',
            direction: 'DESC'
        }
    }) => Bacon.update(initialState,
        [d.stream('activate')], (current) => ({...current, active: true}),
        [d.stream('changeParams')], (current, params) => ({...current, params})
    ),
    order: (by, direction='ASC') => d.push('changeParams', {
        sortBy: by,
        direction: direction
    }),
    activate: () => d.push('activate')
};

export const kutsuListP = kutsuList.toProperty();
export const kutsuListRequestS = Bacon.combineTemplate({state: kutsuListP}).changes();
export const kutsuListResponseS = kutsuListRequestS.skipWhile(stateHolder => !stateHolder.state.active)
    .flatMap(stateHolder => Bacon.fromPromise(fetch(KUTSU_URL+queryString(stateHolder.state.params))
            .then(response => response.json().then(json => ({loaded: true, result: json}))
                    .catch(no => ({loaded:false, result:null}))))
    );
export default kutsuList
