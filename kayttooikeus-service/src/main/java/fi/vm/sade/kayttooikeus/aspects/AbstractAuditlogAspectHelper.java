package fi.vm.sade.kayttooikeus.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusLogMessage;
import fi.vm.sade.kayttooikeus.service.OmatTiedotService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractAuditlogAspectHelper {

    private final OmatTiedotService omatTiedotService;
    private final Audit audit;
    private final ObjectMapper mapper;

    void finishLogging(KayttoOikeusLogMessage.LogMessageBuilder builder) {
        String oid = omatTiedotService.getCurrentUserOid();
        KayttoOikeusLogMessage message = builder.id(oid).build();
        audit.log(message);
    }

    ObjectMapper getObjectMapper() {
        return mapper;
    }
}
