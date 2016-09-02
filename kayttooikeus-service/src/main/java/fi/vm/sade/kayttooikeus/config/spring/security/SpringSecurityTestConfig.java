package fi.vm.sade.kayttooikeus.config.spring.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 17.34
 */
@Configuration
@Profile({"test", "itest"})
@EnableWebMvcSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true,
        proxyTargetClass = true, jsr250Enabled = true)
public class SpringSecurityTestConfig {
}
