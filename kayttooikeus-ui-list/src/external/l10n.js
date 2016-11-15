import Bacon from 'baconjs'
import R from 'ramda'

const localeBus = new Bacon.Bus();
export const localeP = localeBus.toProperty('fi');

const l10nRequestS = Bacon.fromCallback(callback => setTimeout(() => callback(window.url('kayttooikeus-service.l10n')),0));
const l10nResponseS = l10nRequestS.flatMap(url => Bacon.fromPromise(fetch(url).then(response =>  response.json())));
const lokalisointiResponseS = l10nResponseS.flatMap(defaultsJson => Bacon.fromPromise(fetch(window.url('lokalisointi.localisation'))
    .then(response => response.json().then(json => {
        const byLocale = {...defaultsJson};
        R.forEach(row => (byLocale[row.locale] || (byLocale[row.locale] = {}))[row.key.toUpperCase()] = row.value, json);
        return byLocale;
    }))));
export const l10nByLocaleP = lokalisointiResponseS.toProperty(); 

export const l10nP = Bacon.combineWith(l10nByLocaleP, localeP, (l10n, locale) => l10n[locale]);
export default l10nResponseS
