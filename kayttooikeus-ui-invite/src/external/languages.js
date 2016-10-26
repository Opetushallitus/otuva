import Bacon from 'baconjs'
// remove mocking when ready get real data from backend
import fetchMock from 'fetch-mock'

const LANGS_URL = 'http://languages'
const MOCK_DELAY = 800

fetchMock.get(LANGS_URL, 
  new Promise(res => setTimeout(res, MOCK_DELAY)).then(() => [
    { code: 'fi', "name-fi": 'suomi' },
    { code: 'sv', "name-fi": 'ruomi' },
    { code: 'en', "name-fi": 'englanti' }
  ]
))

const fetchFromUrl = url => {
  return Bacon.fromPromise(
    fetch(url)
      .then(response => {
        return response.json()
      })
  )
}

const langsRequestS = Bacon.later(0, LANGS_URL)
const langsResponseS = langsRequestS.flatMap(fetchFromUrl)

export const langsResponsePendingP = langsRequestS.awaiting(langsResponseS)
export default langsResponseS
