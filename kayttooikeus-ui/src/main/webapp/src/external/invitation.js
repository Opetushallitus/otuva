import Bacon from 'baconjs'
// remove mocking when ready get real data from backend
import fetchMock from 'fetch-mock'

const INVITATION_URL = 'http://invitation'
const MOCK_DELAY = 3000

fetchMock.post(INVITATION_URL, 
  new Promise(res => setTimeout(res, MOCK_DELAY)).then(() => 201)
)

const fetchFromUrl = ({url, payload}) => {
  return Bacon.fromPromise(
    fetch(url, {
      method: 'POST',
      body: payload
    })
  )
}

export default function invite(payload) {
  const invitationRequestS = Bacon.later(0, { 
    url: INVITATION_URL, payload
  })
  const invitationResponseS = invitationRequestS.flatMap(fetchFromUrl)  
  const invitationResponsePendingP = invitationRequestS
    .awaiting(invitationResponseS)
 
  return {
    invitationResponseS,
    invitationResponsePendingP
  }
}
