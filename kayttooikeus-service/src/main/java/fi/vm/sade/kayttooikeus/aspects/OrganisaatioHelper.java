package fi.vm.sade.kayttooikeus.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusLogMessage;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusOperation;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import org.springframework.stereotype.Component;

@Component
public class OrganisaatioHelper extends AbstractAuditlogAspectHelper {

    public OrganisaatioHelper(PermissionCheckerService omatTiedotService, Audit audit, ObjectMapper mapper) {
        super(omatTiedotService, audit, mapper);
    }

    void logUpdateOrganisationCache() {
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .lisatieto("Päivitetty organisaatiovälimuisti.")
                .setOperaatio(KayttoOikeusOperation.UPDATE_ORGANISAATIO_CACHE);
        finishLogging(logMessage);
    }

}
