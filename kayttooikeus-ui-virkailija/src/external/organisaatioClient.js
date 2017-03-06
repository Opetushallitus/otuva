import http from "../external/http"

export const organisationByOid = oid => {
    return http.get(window.url('organisaatio-service.organisaatio.ByOid', oid));
};
