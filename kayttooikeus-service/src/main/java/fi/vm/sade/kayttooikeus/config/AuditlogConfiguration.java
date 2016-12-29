package fi.vm.sade.kayttooikeus.config;

import fi.vm.sade.auditlog.ApplicationType;
import fi.vm.sade.auditlog.Audit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditlogConfiguration {

    private final String SERVICE_NAME = "kayttooikeus-service";

    @Bean
    public Audit audit() {
        return new Audit(SERVICE_NAME, ApplicationType.VIRKAILIJA);
    }

}
