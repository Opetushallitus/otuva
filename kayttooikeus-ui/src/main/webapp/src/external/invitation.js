import Bacon from 'baconjs'
// remove mocking when ready get real data from backend
import fetchMock from 'fetch-mock'

const INVITATION_URL = 'http://invitation'
const MOCK_DELAY = 1600

const fetchFromUrl = ({url, payload}) => {
  return Bacon.fromPromise(
    fetch(url, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
      .then(response => response.json())
  )
}

export default function invite(payload) {
  fetchMock.post(INVITATION_URL, 
    new Promise(res => setTimeout(res, MOCK_DELAY))
      .then((res) => ({
        status: 201,
        body: payload
      }))
  )

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
