import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import 'oph-urls-js'  // functions (urls, url, etc.) attached to window

import l10nResponseS, { l10nResponsePendingP } from './external/l10n'
import MainContainer from './components/MainContainer.js'
import view from './logic/view.js'
import { buildVersionResponsePendingP } from './external/buildversion'
import { kutsuListP, kutsuListResponseS } from './external/kutsuList'

import './reset.css'
import './index.css'

const appStateS = Bacon.combineTemplate({
    l10n: l10nResponseS.toProperty(),
    view: view.toProperty(),
    kutsuListState: kutsuListP,
    kutsuList: kutsuListResponseS.toProperty({loaded:false, result:[]})
}).changes();

appStateS
  .skipWhile(l10nResponsePendingP
        .or(buildVersionResponsePendingP))
  .onValue(appState => {
    ReactDOM.render(
      <MainContainer {...appState} />, document.getElementById('root')
    );
  });