package fi.vm.sade.kayttooikeus.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusLogMessage;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusOperation;
import org.springframework.stereotype.Component;

@Component
public class OrganisaatioHelper extends AbstractAuditlogAspectHelper {

    public OrganisaatioHelper(Audit audit, ObjectMapper mapper) {
        super(audit, mapper);
    }

    void logUpdateOrganisationCache() {
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .lisatieto("Päivitetty organisaatiovälimuisti.")
                .setOperaatio(KayttoOikeusOperation.UPDATE_ORGANISAATIO_CACHE);
        finishLogging(logMessage);
    }

}
