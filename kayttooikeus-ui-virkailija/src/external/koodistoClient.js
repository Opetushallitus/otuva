import Bacon from "baconjs";
import {handleFetchError} from "../logic/fetchUtils";
import {commonHandleError} from "../logic/error";
import {l10nP} from "./l10n";

// l10nP just to wait until frontProperties have been loaded
export const koodistoKieliP = Bacon.combineWith(l10nP, l10n => {
    const koodistoKieliS = Bacon.fromPromise(fetch(window.url('koodisto-service.koodisto.kieli'),
        {credentials: 'same-origin'})
        .then(handleFetchError)
        .then(response => response.json().then(json => ({loaded: true, result: json}))
            .catch(e => console.error(e))));
    koodistoKieliS.onError(commonHandleError);
    return koodistoKieliS;
}).flatMap(r => r);

// l10nP just to wait until frontProperties have been loaded
export const koodistoKansalaisuusP = Bacon.combineWith(l10nP, l10n => {
    const koodistoKansalaisuusS = Bacon.fromPromise(fetch(window.url('koodisto-service.koodisto.kansalaisuus'),
        {credentials: 'same-origin'})
        .then(handleFetchError)
        .then(response => response.json().then(json => ({loaded: true, result: json}))
            .catch(e => console.error(e))));
    koodistoKansalaisuusS.onError(commonHandleError);
    return koodistoKansalaisuusS;
}).flatMap(r => r);

