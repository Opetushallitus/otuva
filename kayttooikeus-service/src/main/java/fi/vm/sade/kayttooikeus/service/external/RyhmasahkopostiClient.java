package fi.vm.sade.kayttooikeus.service.external;

import fi.vm.sade.kayttooikeus.model.email.EmailData;

public interface RyhmasahkopostiClient {
    /**
     * @param emailData to send
     * @return response from ryhmasahkoposti-service
     */
    String sendRyhmasahkoposti(EmailData emailData);
}
