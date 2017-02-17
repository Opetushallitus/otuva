import Bacon from "baconjs";
import {handleFetchError} from "../logic/fetchUtils";
import {commonHandleError} from "../logic/error";

export const organisationByOid = oid => {
    const response = Bacon.fromPromise(fetch(window.url('organisaatio-service.organisaatio.ByOid', oid),
        { credentials: 'same-origin',})
        .then(handleFetchError)
        .then(response => response.json().then(json => ({loaded: true, result: json}))));
    response.onError(commonHandleError);
    return response;
};
