import Bacon from "baconjs";
import {urlsP} from "../external/urls";

const omatOidRequestS = Bacon.combineWith(urlsP, urls => urls.url('kayttooikeus-service.omattiedot.oid')).toEventStream();
export const omaOidResponseS = omatOidRequestS.flatMap(url => Bacon.fromPromise(fetch(url, {credentials: 'same-origin'})
    .then(response => response.text())));
export const omaOidP = omaOidResponseS.toProperty();
