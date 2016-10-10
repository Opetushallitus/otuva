package fi.vm.sade.kayttooikeus.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by autio on 29.9.2016.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "cas", ignoreUnknownFields = false)
public class CasProperties {
    @Getter
    @Setter
    public static class Ldap {
        private String url;
        private String managedDn;
        private String password;
        private String userSearchBase;
        private String userSearchFilter;
    }
    private Ldap ldap;

    private String service;
    private Boolean sendRenew;
    private String key;
    private String url;
    private String fallbackUserDetailsProviderUrl;
}