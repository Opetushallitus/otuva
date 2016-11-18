import Bacon from "baconjs";
import {urlsP} from "./urls";

const fetchFromUrl = url => Bacon.fromPromise(fetch(url).then(response => response));

const buildVersionRequestS = Bacon.combineWith(urlsP, urls => urls.url('kayttooikeus-service.buildversion')).toEventStream();
const buildVersionResponseS = buildVersionRequestS.flatMap(fetchFromUrl);

export default buildVersionResponseS