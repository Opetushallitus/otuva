package fi.vm.sade.kayttooikeus.aspects;

import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusLogMessage;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusOperation;
import org.springframework.stereotype.Component;

@Component
public class OrganisaatioHelper extends AuditlogAspectHelper {

    void logUpdateOrganisationCache() {
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .lisatieto("Päivitetty organisaatiovälimuisti.")
                .setOperaatio(KayttoOikeusOperation.UPDATE_ORGANISAATIO_CACHE);
        finishLogging(logMessage);
    }

}
