package fi.vm.sade.kayttooikeus.config;

import fi.vm.sade.kayttooikeus.config.properties.UrlConfiguration;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.impl.OrganisaatioClientImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalClientsConfig {
    private UrlConfiguration urlConfiguration;
    
    @Autowired
    public ExternalClientsConfig(UrlConfiguration urlConfiguration) {
        this.urlConfiguration = urlConfiguration;
    }

    @Bean
    public OrganisaatioClient organisaatioClient() {
        return new OrganisaatioClientImpl(urlConfiguration);
    }
}
