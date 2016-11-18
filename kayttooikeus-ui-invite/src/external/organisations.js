import Bacon from 'baconjs'

import omatTiedotResponseS from './omattiedot'

const orgsResponseS = Bacon.fromPromise(omatTiedotResponseS.toPromise().then(omatTiedot => {
  const henkiloOid = omatTiedot.oidHenkilo
  const ORGS_URL = `/kayttooikeus-service/henkilo/${henkiloOid}/organisaatio`
  return fetch(ORGS_URL, { credentials: 'same-origin' }).then(response => response.json())
}))

export default orgsResponseS
