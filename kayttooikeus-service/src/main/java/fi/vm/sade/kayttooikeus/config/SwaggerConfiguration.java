package fi.vm.sade.kayttooikeus.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@ConditionalOnProperty(name = "kayttooikeus.swagger.enabled", matchIfMissing = true)
public class SwaggerConfiguration {

}
