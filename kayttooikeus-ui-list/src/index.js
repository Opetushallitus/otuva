import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'
import 'oph-urls-js'  // functions (urls, url, etc.) attached to window

import { l10nP } from './external/l10n'
import { contentP } from './logic/route'
import { locationP } from './logic/location'
import TopNavigation from './components/TopNavigation'

import './reset.css'
import './index.css'

const domP = Bacon.combineWith(locationP, l10nP, contentP, (location, l10n, content) => {
    const props = {location, l10n};
    return <div className="mainContainer">
        <TopNavigation {...props}/>
        {content}
    </div>
});
domP.onValue(component => ReactDOM.render(component, document.getElementById('root')));
