package fi.vm.sade.kayttooikeus.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusLogMessage;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RequiredArgsConstructor
public abstract class AbstractAuditlogAspectHelper {

    private final Audit audit;
    private final ObjectMapper mapper;

    private Optional<String> getAuthenticationName() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName);
    }

    void finishLogging(KayttoOikeusLogMessage.LogMessageBuilder builder) {
        getAuthenticationName().ifPresent(builder::id);
        audit.log(builder.build());
    }

    ObjectMapper getObjectMapper() {
        return mapper;
    }
}
