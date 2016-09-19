package fi.vm.sade.kayttooikeus.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 12.58
 */
@Configuration
@Import({SpringSecurityEnvConfig.class, SpringSecurityTestConfig.class})
public class SpringSecurityConfig{
}
