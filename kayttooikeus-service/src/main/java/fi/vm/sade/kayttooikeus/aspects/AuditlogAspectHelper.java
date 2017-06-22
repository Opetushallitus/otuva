package fi.vm.sade.kayttooikeus.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusLogMessage;
import fi.vm.sade.kayttooikeus.config.AuditlogConfiguration;
import fi.vm.sade.kayttooikeus.service.OmatTiedotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuditlogAspectHelper {

    private OmatTiedotService omatTiedotService;
    private Audit audit;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    void setAudit(AuditlogConfiguration configuration) {
        audit = configuration.audit();
    }
    @Autowired
    void setOmatTiedotService(OmatTiedotService service) {
        omatTiedotService = service;
    }

    void finishLogging(KayttoOikeusLogMessage.LogMessageBuilder builder) {
        String oid = omatTiedotService.getCurrentUserOid();
        KayttoOikeusLogMessage message = builder.id(oid).build();
        audit.log(message);
    }

    ObjectMapper getObjectMapper() {
        return mapper;
    }
}
