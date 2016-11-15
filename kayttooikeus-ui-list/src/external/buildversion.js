import Bacon from "baconjs";

const fetchFromUrl = url => Bacon.fromPromise(fetch(url).then(response => response));

const buildVersionRequestS = Bacon.fromCallback(callback => setTimeout(() => callback(window.url('kayttooikeus-service.buildversion')),0));
const buildVersionResponseS = buildVersionRequestS.flatMap(fetchFromUrl);

export const buildVersionResponsePendingP = buildVersionRequestS.awaiting(buildVersionResponseS);
export default buildVersionResponseS
