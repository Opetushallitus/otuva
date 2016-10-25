import Bacon from 'baconjs'
import fetchMock from 'fetch-mock'

const BUILDVERSION_URL = '/kayttooikeus-service/buildversion.txt'

fetchMock
  .spy(BUILDVERSION_URL)

const fetchFromUrl = url => {
  return Bacon.fromPromise(
    fetch(url)
      .then(response => {
        return response
      })
  )
}

const buildVersionRequestS = Bacon.later(0, BUILDVERSION_URL)
const buildVersionResponseS = buildVersionRequestS.flatMap(fetchFromUrl)

export const buildVersionResponsePendingP = 
  buildVersionRequestS.awaiting(buildVersionResponseS)
export default buildVersionResponseS
