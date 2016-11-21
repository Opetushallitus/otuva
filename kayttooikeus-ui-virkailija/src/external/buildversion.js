import Bacon from "baconjs";
import {urlsP} from "./urls";
import {handleError} from "../logic/error";

const buildVersionRequestS = Bacon.combineWith(urlsP, urls => urls.url('kayttooikeus-service.buildversion')).toEventStream();
const buildVersionResponseS = buildVersionRequestS.flatMap(url => 
    Bacon.fromPromise(fetch(url).then(handleError).then(response => response)));

export default buildVersionResponseS