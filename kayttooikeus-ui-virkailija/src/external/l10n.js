import Bacon from 'baconjs'
import R from 'ramda'
import {urlsP} from './urls'
import {handleFetchError} from "../logic/fetchUtils";

const localeBus = new Bacon.Bus();
export const localeP = localeBus.toProperty('fi');

const l10nRequestS = Bacon.combineWith(urlsP, urls => urls.url('kayttooikeus-service.l10n')).toEventStream();
const l10nResponseS = l10nRequestS.flatMap(url => Bacon.fromPromise(fetch(url).then(handleFetchError).then(response =>  response.json())));
const lokalisointiRequestS = Bacon.combineWith(urlsP, urls => urls.url('lokalisointi.localisation')).toEventStream();
const lokalisointiResponseS = lokalisointiRequestS.flatMap(url => Bacon.fromPromise(fetch(url).then(handleFetchError).then(response =>  response.json())));
const l10nByLocaleP = Bacon.combineWith(l10nResponseS.toProperty(), lokalisointiResponseS.toProperty(),
    (defaultsJson, json) => {
        const byLocale = {...defaultsJson};
        R.forEach(row => (byLocale[row.locale] || (byLocale[row.locale] = {}))[row.key.toUpperCase()] = row.value, json);
        return byLocale;
    });
export const l10nP = Bacon.combineWith(l10nByLocaleP, localeP, (l10n, locale) => ({...l10n[locale], msg:function(key, ...params) {
    var msg = l10n[locale][key], i = 1;
    R.forEach((param) => msg = !msg ? null : (""+msg).replace(new RegExp('\\{'+(i++)+'\\}', 'g'), param), params);
    return msg;
}}));
export default l10nResponseS