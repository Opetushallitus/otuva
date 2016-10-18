import Bacon from 'baconjs'
// remove mocking when ready get real data from backend
import fetchMock from 'fetch-mock'

const ORGS_URL = 'http://orgs'
const MOCK_DELAY = 800

fetchMock.get(ORGS_URL, 
  new Promise(res => setTimeout(res, MOCK_DELAY)).then(() => [
    { 
      id: 'org1', 
      "name-fi": 'Org 1', 
      permissions: [
        { id: 'org1perm1', "name-fi": 'Org1 Perm 1'},
        { id: 'org1perm2', "name-fi": 'Org1 Perm 2'},
        { id: 'org1perm3', "name-fi": 'Org1 Perm 3'},
      ]
    },
    { 
      id: 'org2', 
      "name-fi": 'Org 2',
      permissions: [
        { id: 'org2perm1', "name-fi": 'Org2 Perm 1'},
        { id: 'org2perm2', "name-fi": 'Org2 Perm 2'},
        { id: 'org2perm3', "name-fi": 'Org2 Perm 3'},
      ]
    },
    { 
      id: 'org3', 
      "name-fi": 'Org 3',
      permissions: [
        { id: 'org3perm1', "name-fi": 'Org3 Perm 1'},
        { id: 'org3perm2', "name-fi": 'Org3 Perm 2'},
        { id: 'org3perm3', "name-fi": 'Org3 Perm 3'},
      ]
    }
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

const orgsRequestS = Bacon.later(0, ORGS_URL)
const orgsResponseS = orgsRequestS
  .flatMap(fetchFromUrl)

export const orgsResponsePendingP = orgsRequestS.awaiting(orgsResponseS)
export default orgsResponseS