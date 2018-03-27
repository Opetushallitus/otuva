package fi.vm.sade.kayttooikeus.config;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import fi.vm.sade.kayttooikeus.service.exception.DataInconsistencyException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration;
import org.springframework.boot.autoconfigure.ldap.LdapProperties;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.util.StringUtils;

/**
 * Ylikirjoittaa
 * {@link org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapAutoConfiguration#ldapContextSource()}:n
 * beanin jotta saadaan testien {@link LdapContextSource kontekstin} base-dn
 * konfiguroitua.
 */
@Configuration
@EnableConfigurationProperties({LdapProperties.class, EmbeddedLdapProperties.class})
@AutoConfigureBefore(LdapAutoConfiguration.class)
@ConditionalOnClass(InMemoryDirectoryServer.class)
@ConditionalOnProperty(prefix = "spring.ldap.embedded", name = "enabled")
@RequiredArgsConstructor
public class EmbeddedLdapConfiguration {

    private final Environment environment;
    private final LdapProperties properties;
    private final EmbeddedLdapProperties embeddedProperties;

    @Bean
    @DependsOn("directoryServer")
    public ContextSource contextSource() {
        LdapContextSource source = new LdapContextSource();
        if (hasCredentials(this.embeddedProperties.getCredential())) {
            source.setUserDn(this.embeddedProperties.getCredential().getUsername());
            source.setPassword(this.embeddedProperties.getCredential().getPassword());
        }
        source.setUrls(this.properties.determineUrls(this.environment));
        source.setBase(embeddedProperties.getBaseDn().stream().findFirst().orElseThrow(DataInconsistencyException::new));
        return source;
    }

    private boolean hasCredentials(EmbeddedLdapProperties.Credential credential) {
        return StringUtils.hasText(credential.getUsername())
                && StringUtils.hasText(credential.getPassword());
    }

}
