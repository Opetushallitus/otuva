import Bacon from 'baconjs'

import { routeErrorP } from './route'
import dispatcher from './dispatcher'

const logError = (error) => {
    console.log('ERROR', error);
};

const globalErrorDispatcher = dispatcher();
const globalErrorP = Bacon.update({},
    [globalErrorDispatcher.stream('set')], (current, newError) => ({...newError}),
    [globalErrorDispatcher.stream('clear')], () => ({})
);

const errorText = (code, l10n) => l10n['ERROR_'+code] || {
    400: 'Järjestelmässä tapahtui odottamaton virhe. Yritä myöhemmin uudelleen.',
    404: 'Etsimääsi sivua ei löytynyt',
    409: 'Muutoksia ei voida tallentaa, koska toinen käyttäjä on muuttanut tietoja sivun latauksen jälkeen. Lataa sivu uudelleen.',
    500: 'Järjestelmässä tapahtui odottamaton virhe. Yritä myöhemmin uudelleen.',
    503: 'Palvelimeen ei saatu yhteyttä. Yritä myöhemmin uudelleen.'
}[code];

export const errorPF = (stateP, l10nP) => {
    const stateErrorP = stateP.changes().errors()
        .mapError(error => ({ httpStatus: error.httpStatus }))
        .flatMap(e => Bacon.combineWith(l10nP, l10n => ({error: e, l10n: l10n})))
        .flatMap(({error,l10n}) => Bacon.once(error).concat(errorText(error.httpStatus, l10n)
            ? Bacon.fromEvent(document.body, 'click').map({}) // Retryable errors can be dismissed
            : Bacon.never()
        )).toProperty({});
    return Bacon.combineWith(stateErrorP, routeErrorP, globalErrorP, l10nP, (error, routeError, globalError, l10n) => {
        return error.httpStatus ? error : ((globalError.message || globalError.messageKey) ? {
            type: globalError.type || 'error',
            comment: (globalError.message && l10n[globalError.message.toUpperCase()]) || globalError.message
                    || (globalError.messageKey && l10n[globalError.messageKey.toUpperCase()])
                    || (globalError.status && errorText(globalError.status, l10n))
        } : routeError);
    });
};

export const commonHandleError = (error) => {
    if (requiresLogin(error)) {
        document.location = window.url('cas.login');
    } else {
        logError(error);
        error.json().then(json => globalErrorDispatcher.push('set',json));
    }
};

export function clearGlobalErrors() {
    globalErrorDispatcher.push('clear');
}
export function setSuccess(msg) {
    globalErrorDispatcher.push('set', {type: 'success', message:msg});
}

export function requiresLogin(e) {
    return e.status === 401 || e.status === 403;
}
