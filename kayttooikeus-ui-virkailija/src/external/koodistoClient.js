import Bacon from "baconjs";
import {handleFetchError} from "../logic/fetchUtils";
import {commonHandleError} from "../logic/error";
import {urlsP} from "../external/urls";

// l10nP just to wait until frontProperties have been loaded
function httpGet(url) {
    const x = urlsP.flatMap(param =>
        Bacon.fromPromise(fetch(window.url(url),
            {credentials: 'same-origin'})
            .then(handleFetchError)
            .then(response => response.json().then(json => ({loaded: true, result: json}))
                .catch(e => console.error(e))))
    ).toProperty();
    x.onError(commonHandleError);
    return x;
}
export const koodistoKansalaisuusP = httpGet('koodisto-service.koodisto.kansalaisuus');
export const koodistoKieliP = httpGet('koodisto-service.koodisto.kieli');
export const koodistoSukupuoliP = httpGet('koodisto-service.koodisto.sukupuoli');
export const koodistoYhteystietotyypitP = httpGet('koodisto-service.koodisto.yhteystietotyypit');
