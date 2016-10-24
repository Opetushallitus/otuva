package fi.vm.sade.kayttooikeus.service.external;

import fi.vm.sade.ryhmasahkoposti.api.dto.EmailData;
import org.apache.http.HttpResponse;

public interface RyhmasahkopostiClient {
    /**
     * @param emailData to send
     * @return response from ryhmasahkoposti-service
     * @throws ExternalServiceException on failure
     */
    HttpResponse sendRyhmasahkoposti(EmailData emailData);
}
