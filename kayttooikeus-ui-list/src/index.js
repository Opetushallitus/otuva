import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import 'oph-urls-js'  // functions (urls, url, etc.) attached to window

import l10nResponseS, { l10nResponsePendingP } from './external/l10n'
import MainContainer from './components/MainContainer.js'
import view from './logic/view.js'
import buildVersionResponseS, { buildVersionResponsePendingP } from './external/buildversion'

import './reset.css'
import './index.css'

const appStateS = Bacon.combineTemplate({
    l10n: l10nResponseS.toProperty(),
    view: view.toProperty()
}).changes();

appStateS
  .skipWhile(l10nResponsePendingP
        .or(buildVersionResponsePendingP))
  .onValue(appState => {
    ReactDOM.render(
      <MainContainer {...appState} />, document.getElementById('root')
    );
  });

buildVersionResponseS.onValue(res => { console.log(res) });
buildVersionResponseS.onError(err => { console.log(err) });