import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'

import organisations from './logic/organisations'
import basicInfo from './logic/basicInfo'
import l10nResponseS, { l10nResponsePendingP } from './external/l10n'
import orgsResponseS, { orgsResponsePendingP } from './external/organisations'
import langResponseS, { langResponsePendingP } from './external/languages'
import buildVersionResponseS from './external/buildversion'
import InvitationForm from './components/InvitationForm'

import './reset.css'
import './index.css'

const appStateS = Bacon.combineTemplate({
  addedOrgs: organisations.toProperty(),
  basicInfo: basicInfo.toProperty(),
  l10n: l10nResponseS.toProperty(),
  orgs: orgsResponseS.toProperty(),
  languages: langResponseS.toProperty(),
}).changes()

appStateS
  .skipWhile(orgsResponsePendingP
    .or(l10nResponsePendingP)
    .or(langResponsePendingP))
  .onValue(appState => {
    ReactDOM.render(
      <InvitationForm {...appState} />, document.getElementById('root')
    )
  })

buildVersionResponseS.onValue(res => { console.log(res) })
buildVersionResponseS.onError(err => { console.log(err) })
