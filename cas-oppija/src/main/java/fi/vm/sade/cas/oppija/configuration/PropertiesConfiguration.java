package fi.vm.sade.cas.oppija.configuration;

import fi.vm.sade.properties.OphProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class PropertiesConfiguration {

    @Bean
    public OphProperties ophProperties(Environment environment) {
        OphProperties properties = new OphProperties("/cas-oppija-oph.properties");
        properties.addOverride("host.virkailija", environment.getRequiredProperty("host.virkailija"));
        return properties;
    }

}
