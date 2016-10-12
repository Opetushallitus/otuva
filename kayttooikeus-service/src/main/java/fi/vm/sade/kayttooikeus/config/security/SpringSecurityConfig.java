package fi.vm.sade.kayttooikeus.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * Created by autio on 19.9.2016.
 */
@Configuration
@Import({SecurityConfigDev.class, SecurityConfigDefault.class})
public class SpringSecurityConfig{
}
