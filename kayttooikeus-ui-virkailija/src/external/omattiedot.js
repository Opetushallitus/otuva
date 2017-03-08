import Bacon from "baconjs";
import {urlsP} from "../external/urls";

const omatOidRequestS = Bacon.combineWith(urlsP, urls => {
    return urls.url('kayttooikeus-service.omattiedot.oid')
}).toEventStream();

const omaOidResponseS = omatOidRequestS.flatMap(url => {
    return Bacon.fromPromise(fetch(url, {credentials: 'same-origin'})
        .then(response => response.text()))
});
export const omaOidP = omaOidResponseS.toProperty();
