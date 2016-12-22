package fi.vm.sade.kayttooikeus.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "common")
public class CommonProperties {
    private String rootOrganizationOid;
    private String groupOrganizationId;
    private InvitationEmail invitationEmail;
    
    @Getter @Setter
    public static class InvitationEmail {
        private String template;
        private String from;
        private String sender;
    }
}