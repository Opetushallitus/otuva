package fi.vm.sade.kayttooikeus.aspects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusLogMessage;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusOperation;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeusCreateDto;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeusRyhmaModifyDto;
import org.springframework.stereotype.Component;

@Component
public class KayttoOikeusRyhmaHelper extends AbstractAuditlogAspectHelper {

    public KayttoOikeusRyhmaHelper(Audit audit, ObjectMapper mapper) {
        super(audit, mapper);
    }

    void logCreateKayttooikeusryhma(KayttoOikeusRyhmaModifyDto ryhma, Object result) {
        String newGroup;
        try {
            newGroup = getObjectMapper().writeValueAsString(ryhma);
        } catch (JsonProcessingException e) {
            newGroup = "KayttoOikeusRyhmaModifyDto:n muuttaminen JSON muotoon epäonnistui!";
        }

        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .newValue(newGroup)
                .lisatieto("Luotu uusi käyttöoikeusryhmä.")
                .setOperaatio(KayttoOikeusOperation.CREATE_KAYTTOOIKEUSRYHMA);
        finishLogging(logMessage);
    }

    void logCreateKayttooikeus(KayttoOikeusCreateDto kayttoOikeus, Object result) {
        String newValue;
        try {
            newValue = getObjectMapper().writeValueAsString(kayttoOikeus);
        } catch (JsonProcessingException e) {
            newValue = "KayttoOikeusCreateDto:n muuttaminen JSON muotoon epäonnistui!";
        }

        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .newValue(newValue)
                .lisatieto("Luotu uusi käyttöoikeus annetun käyttöoikeusryhmän pohjalta.")
                .setOperaatio(KayttoOikeusOperation.CREATE_KAYTTOIKEUS);
        finishLogging(logMessage);
    }

    void logUpdateKayttoOikeusForKayttoOikeusRyhma(long id, KayttoOikeusRyhmaModifyDto ryhmaData, Object result) {
        String updatedGroup;
        try {
            updatedGroup = getObjectMapper().writeValueAsString(ryhmaData);
        } catch (JsonProcessingException e) {
            updatedGroup = "KayttoOikeusRyhmaModifyDto:n muuttaminen JSON muotoon epäonnistui!";
        }

        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .newValue(updatedGroup)
                .lisatieto("Käyttöoikeusryhmän käyttöoikeudet päivitetty.")
                .setOperaatio(KayttoOikeusOperation.UPDATE_KAYTTOOIKEUSRYHMA);
        finishLogging(logMessage);
    }


}
