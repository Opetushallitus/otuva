package fi.vm.sade.kayttooikeus.config.spring.mvc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 12.56
 */
@Configuration
@EnableWebMvc
@Import(SwaggerConfig.class)
@ComponentScan("fi.vm.sade.kayttooikeus.mvc")
public class SpringMvcConfig {
}
