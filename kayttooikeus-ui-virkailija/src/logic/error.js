import { routeErrorP } from './route'
import Bacon from 'baconjs'

const logError = (error) => {
    console.log('ERROR', error)
};

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
    return Bacon.combineWith(stateErrorP, routeErrorP, (error, routeError) =>
        error.httpStatus ? error : routeError
    );
};

export const handleError = (error) => {
    if (requiresLogin(error)) {
        document.location = window.url('cas.login');
    } else {
        logError(error)
    }
};

export function requiresLogin(e) {
    return e.httpStatus === 401 || e.httpStatus === 403;
}
