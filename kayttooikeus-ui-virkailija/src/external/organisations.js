import Bacon from 'baconjs'

import {omaOidP} from './omattiedot'
import {handleFetchError} from '../logic/fetchUtils'

const orgsResponseS = omaOidP.changes().flatMap(henkiloOid =>
    Bacon.fromPromise(fetch(window.url('kayttooikeus-service.henkilo.organisaatios', henkiloOid), {credentials: 'same-origin'})
        .then(handleFetchError).then(response => response.json())));
export default orgsResponseS
export const orgsP = orgsResponseS.toProperty();