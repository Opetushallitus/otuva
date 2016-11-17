import Bacon from 'baconjs'

// TODO: use kayttooikeus-service
const OMAT_TIEDOT_URL = '/authentication-service/resources/omattiedot'

const fetchFromUrl = url => {
  return Bacon.fromPromise(
    fetch(url, {
        credentials: 'same-origin'
      })
      .then(response => {
        return response.json()
      })
  )
}

const omatTiedotRequestS = Bacon.later(0, OMAT_TIEDOT_URL)
const omatTiedotResponseS = omatTiedotRequestS.flatMap(fetchFromUrl)

export const omatTiedotResponsePendingP = omatTiedotRequestS.awaiting(omatTiedotResponseS)
export default omatTiedotResponseS
