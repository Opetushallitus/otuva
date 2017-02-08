import Bacon from "baconjs";
import {urlsP} from "../external/urls";
import dispatcher from "../logic/dispatcher";
import {handleFetchError} from "../logic/fetchUtils";
import {commonHandleError} from "../logic/error";

const henkiloDispatcher = dispatcher();



export const fetchHenkiloByOid = oid =>
    Bacon.fromPromise(fetch(window.url('oppijanumerorekisteri-service.henkilo.oid', oid), {credentials: 'same-origin'})
        .then(response => response.text())).then(handleFetchError).then(response => {
        kutsuList.update();
        return response;
    });

