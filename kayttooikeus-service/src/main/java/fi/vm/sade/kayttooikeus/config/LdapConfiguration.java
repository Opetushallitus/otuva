package fi.vm.sade.kayttooikeus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.ldap.core.support.BaseLdapPathBeanPostProcessor;

@Configuration
@EnableLdapRepositories
public class LdapConfiguration {

    @Bean
    public BaseLdapPathBeanPostProcessor baseLdapPathBeanPostProcessor() {
        return new BaseLdapPathBeanPostProcessor();
    }

}
