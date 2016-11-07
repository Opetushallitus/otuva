import React from 'react'
import ReactDOM from 'react-dom'

import InvitationForm from './InvitationForm'

const appState = {
  addedOrgs: [],
  basicInfo: {},
  l10n: {
    'fi': { }
  },
  orgs: {
    numHits: 1,
    organisaatiot: [
      {oid: 1, organisaatiotyypit: []},
    ]
  },
  permissions: [
  ],
  languages: [{ code: ''}]
}

it('renders without crashing', () => {
  const div = document.createElement('div')
  ReactDOM.render(<InvitationForm {...appState} />, div)
})
