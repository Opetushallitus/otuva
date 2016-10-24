package fi.vm.sade.kayttooikeus.config.properties;

import fi.vm.sade.properties.OphProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


@Configuration
public class UrlConfiguration extends OphProperties{

    @Autowired
    public UrlConfiguration(Environment environment) {
        addFiles("/kayttooikeus-service-oph.properties");
        addOptionalFiles(environment.getProperty("spring.config.location"));
    }
}
