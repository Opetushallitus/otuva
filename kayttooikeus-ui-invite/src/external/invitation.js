import Bacon from 'baconjs'

const INVITATION_URL = '/kayttooikeus-service/kutsu'

const fetchFromUrl = ({url, payload}) => {
  return Bacon.fromPromise(
    fetch(url, {
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'same-origin',
      method: 'POST',
      body: JSON.stringify(payload)
    })
      .then(response => response.json())
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
