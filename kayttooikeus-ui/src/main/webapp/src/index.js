import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
// enable polyfill when ready eject from create-react-app (https://github.com/github/fetch)
// import fetch from 'whatwg-fetch'

import organisations from './logic/organisations'
import basicInfo from './logic/basicInfo'
import l10nResponseS, { l10nResponsePendingP } from './external/l10n'
import orgsResponseS, { orgsResponsePendingP } from './external/organisations'
import InvitationForm from './components/InvitationForm'

import './reset.css'
import './index.css'

const appState = Bacon.combineTemplate({
  addedOrgs: organisations.toProperty([]),
  basicInfo: basicInfo.toProperty({}),
  l10n: l10nResponseS.toProperty(),
  orgs: orgsResponseS.toProperty(),
})

const staticData = {
  languages: [
    { code: 'fi', "name-fi": 'suomi' },
    { code: 'sv', "name-fi": 'ruomi' },
    { code: 'en', "name-fi": 'englanti' }
  ],
} 

appState
  .changes()
  .skipWhile(orgsResponsePendingP.or(l10nResponsePendingP))
  .onValue(appState => {
    console.log(appState)
    ReactDOM.render(
      <InvitationForm {...staticData} {...appState} />,
      document.getElementById('root')
    )
  })
