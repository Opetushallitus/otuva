import 'oph-urls-js'  // functions (urls, url, etc.) attached to window

import React from 'react'
import ReactDOM from 'react-dom'
import Bacon from 'baconjs'

import { l10nP } from './external/l10n'
import { urlsP } from './external/urls'
import { buildVersionP } from './external/buildversion'
import { routeP } from './logic/route'
import { locationP } from './logic/location'
import { errorPF, commonHandleError } from './logic/error'
import TopNavigation from './components/TopNavigation'

import './reset.css'
import 'font-awesome-webpack'
import './general-styles.css';
import 'virkailija-style-guide/oph-styles.css'
import './index.css'

const errorP = errorPF(routeP.map('.content'), l10nP);
const domP = Bacon.combineWith(buildVersionP, urlsP, locationP, l10nP, errorP, routeP,
    (buildVersion, urls, location, l10n, error, route) => {
    const props = {location, l10n};
    document.body.style.backgroundColor = route.backgroundColor ? route.backgroundColor : "white";
    return <div className="mainContainer oph-typography">
        <TopNavigation {...props} items={route.navi} oid={location.params['oid']} />
        {error && (error.httpStatus || error.comment) && <div className={(error.type || 'error is-error ')+' topError'}>
            {error.comment}
        </div>}
        <div className="mainContent">
            {route.content}
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
