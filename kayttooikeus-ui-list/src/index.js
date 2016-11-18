import 'oph-urls-js'; // functions (urls, url, etc.) attached to window

import Bacon from 'baconjs'
import React from 'react'
import ReactDOM from 'react-dom'

import { l10nP } from './external/l10n'
import { urlsP } from './external/urls'
import { contentP } from './logic/route'
import { locationP } from './logic/location'
import { errorPF, handleError } from './logic/error'
import TopNavigation from './components/TopNavigation'

import './reset.css'
import './index.css'

const errorP = errorPF(contentP, l10nP);
const domP = Bacon.combineWith(urlsP, locationP, l10nP, errorP, contentP, (urls, location, l10n, error, content) => {
    const props = {location, l10n};
    return <div className="mainContainer">
        {error && error.httpStatus && <div className="error is-error">
            {error.comment}
        </div>}
        <TopNavigation {...props}/>
        {content}
    </div>
});
domP.onValue(component => ReactDOM.render(component, document.getElementById('root')));
domP.onError(handleError);
