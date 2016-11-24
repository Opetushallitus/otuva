package fi.vm.sade.kayttooikeus.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "common", ignoreUnknownFields = false)
public class CommonProperties {
    private String rootOrganizationOid;
    private String groupOrganizationId;
}
