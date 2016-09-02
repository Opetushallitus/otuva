package fi.vm.sade.kayttooikeus.config.spring.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 13.00
 */
@Configuration
@Profile("default")
@ImportResource("classpath:security-config.xml")
public class SpringSecurityEnvConfig {
}
