import Bacon from "baconjs";
import {urlsP} from "../external/urls";
import {clearGlobalErrors, setSuccess} from "./error";

const locationBus = new Bacon.Bus();

function parseQuery(qstr) {
    var query = {};
    var a = qstr.substr(1).split('&');
    for (var i = 0; i < a.length; i++) {
        var b = a[i].split('=');
        query[decodeURIComponent(b[0])] = decodeURIComponent(b[1] || '')
    }
    return query
}

function parseLocation(location) {
    const basePath = window.url('kayttooikeus-service.virkialija-ui.basePath');
    const originalPathName = location.pathname;
    const path = originalPathName.startsWith(basePath) ? originalPathName.substring(basePath.length) : originalPathName;
    return {
        path: path,
        params: parseQuery(location.search),
        queryString: location.search || ''
    }
}

const parsePath = (path) => {
    let a = document.createElement('a');
    a.href = path;
    return parseLocation(a);
};

export const navigateTo = function (path, successMsg=null) {
    if (successMsg) {
        setSuccess(successMsg);
    } else {
        clearGlobalErrors();
    }
    const pathToUse = window.url('kayttooikeus-service.virkialija-ui.basePath')+path;
    history.pushState(null, null, pathToUse);
    locationBus.push(parsePath(pathToUse));
};

window.onpopstate = function() {
    locationBus.push(parseLocation(document.location));
};

urlsP.changes().onValue(urls => locationBus.push(parseLocation(document.location)));

export const locationP = locationBus.toProperty();
export const showError = (error) => locationBus.error(error);
