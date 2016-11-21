import Bacon from 'baconjs'

import {omaOidP} from './omattiedot'
import {handleError} from '../logic/fetchUtils'

const orgsResponseS = omaOidP.changes().flatMap(henkiloOid =>
    Bacon.fromPromise(fetch(window.url('kayttooikeus-service.henkilo.organisaatios', henkiloOid), {credentials: 'same-origin'})
        .then(handleError).then(response => response.json())));
export default orgsResponseS
export const orgsP = orgsResponseS.toProperty();