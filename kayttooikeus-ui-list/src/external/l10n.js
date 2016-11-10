import Bacon from 'baconjs'

const L10N_URL = '/kayttooikeus-service/l10n';

const localeBus = new Bacon.Bus();
const fetchFromUrl = url => Bacon.fromPromise(fetch(url).then(response =>  response.json()));

const l10nRequestS = Bacon.later(0, L10N_URL);
const l10nResponseS = l10nRequestS.flatMap(fetchFromUrl);

export const localeP = localeBus.toProperty('fi');
export const l10nByLocaleP = l10nResponseS.toProperty(); 
export const l10nP = Bacon.combineWith(l10nByLocaleP, localeP, (l10n, locale) => l10n[locale]);
export default l10nResponseS
