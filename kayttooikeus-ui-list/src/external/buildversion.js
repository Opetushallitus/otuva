import Bacon from 'baconjs'

const BUILDVERSION_URL = '/kayttooikeus-service/buildversion.txt';

const fetchFromUrl = url => Bacon.fromPromise(fetch(url).then(response => response));

const buildVersionRequestS = Bacon.later(0, BUILDVERSION_URL);
const buildVersionResponseS = buildVersionRequestS.flatMap(fetchFromUrl);

export const buildVersionResponsePendingP = buildVersionRequestS.awaiting(buildVersionResponseS);
export default buildVersionResponseS
