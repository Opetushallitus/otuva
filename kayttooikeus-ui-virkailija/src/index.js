import 'oph-urls-js'  // functions (urls, url, etc.) attached to window

import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'

import { l10nP } from './external/l10n'
import { urlsP } from './external/urls'
import { buildVersionP } from './external/buildversion'
import { contentP } from './logic/route'
import { locationP } from './logic/location'
import { errorPF, commonHandleError } from './logic/error'
import TopNavigation from './components/TopNavigation'

// TODO: for some reason, does not come from 'select' package in Luokka
import 'select2/dist/css/select2.min.css'
import './reset.css'
import './index.css'
import 'font-awesome-webpack'

const errorP = errorPF(contentP, l10nP);
const domP = Bacon.combineWith(buildVersionP, urlsP, locationP, l10nP, errorP, contentP, (buildVersion, urls, location, l10n, error, content) => {
    const props = {location, l10n};
    return <div className="mainContainer">
        <TopNavigation {...props}/>
        {error && (error.httpStatus || error.comment) && <div className={(error.type || 'error is-error ')+' topError'}>
            {error.comment}
        </div>}
        <div className="mainContent">
            {content}
        </div>
        <div className="appFooter">
            <div className="build">
                Build {buildVersion.branchName} #<span title={buildVersion.buildTtime}>{buildVersion.buildNumber}</span>
            </div>
        </div>
    </div>
});
domP.onValue(component => ReactDOM.render(component, document.getElementById('root')));
domP.onError(commonHandleError);
buildVersionP.onValue(buildVersion => console.info('Backend version: ', buildVersion));
