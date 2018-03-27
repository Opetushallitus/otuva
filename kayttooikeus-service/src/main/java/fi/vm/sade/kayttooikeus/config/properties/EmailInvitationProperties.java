package fi.vm.sade.kayttooikeus.config.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "tasks.email-invitation")
public class EmailInvitationProperties {
    private String senderEmail = "noreply@oph.fi";
}
