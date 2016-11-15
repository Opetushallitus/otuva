import Bacon from 'baconjs'

const ORGS_URL = '/organisaatio-service/rest/organisaatio/v2/hae/tyyppi?aktiiviset=true&suunnitellut=false&lakkautetut=false'

const fetchFromUrl = url => {
  return Bacon.fromPromise(
    fetch(url)
      .then(response => {
        return response.json()
      })
  )
}

const orgsRequestS = Bacon.later(0, ORGS_URL)
const orgsResponseS = orgsRequestS.flatMap(fetchFromUrl)

export const orgsResponsePendingP = orgsRequestS.awaiting(orgsResponseS)
export default orgsResponseS
