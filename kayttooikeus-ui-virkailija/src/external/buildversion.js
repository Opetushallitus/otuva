import Bacon from "baconjs";
import R from 'ramda'
import {urlsP} from "./urls";
import {handleFetchError} from "../logic/fetchUtils";

const buildVersionRequestS = Bacon.combineWith(urlsP, urls => urls.url('kayttooikeus-service.buildversion')).toEventStream();
const buildVersionResponseS = buildVersionRequestS.flatMap(url => 
    Bacon.fromPromise(fetch(url).then(handleFetchError).then(response => response.text())));

export default buildVersionResponseS
export const buildVersionP = buildVersionResponseS.map(infoText => {
    const result = {};
    R.forEach(row => {
        const [key,value] = row.split("=");
        result[key] = value;
    }, infoText.split(/\n\r|\r\n|\r|\n/g));
    return result;
}).toProperty();
