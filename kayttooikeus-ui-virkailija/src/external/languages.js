import Bacon from 'baconjs'

import {urlsP} from './urls'
import {handleFetchError} from '../logic/fetchUtils'

const langsRequestS = Bacon.combineWith(urlsP, urls => urls.url('kayttooikeus-service.l10n.languages')).toEventStream();
const langsResponseS = langsRequestS.flatMap(url => Bacon.fromPromise(fetch(url).then(handleFetchError).then(response => response.json())));
export default langsResponseS
export const languagesP = langsResponseS.toProperty([
        { code: 'fi', name: {fi: 'suomi'} },
        { code: 'sv', name: {fi: 'ruotsi'} },
        { code: 'en', name: {fi: 'englanti'} }
    ]);
