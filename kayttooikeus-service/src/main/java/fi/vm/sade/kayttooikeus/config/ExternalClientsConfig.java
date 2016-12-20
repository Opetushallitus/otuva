package fi.vm.sade.kayttooikeus.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.config.properties.UrlConfiguration;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.impl.OrganisaatioClientImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalClientsConfig {
    private final UrlConfiguration urlConfiguration;
    private final ObjectMapper objectMapper;
    private final CommonProperties commonProperties;
    
    @Autowired
    public ExternalClientsConfig(UrlConfiguration urlConfiguration, ObjectMapper objectMapper, CommonProperties commonProperties) {
        this.urlConfiguration = urlConfiguration;
        this.objectMapper = objectMapper;
        this.commonProperties = commonProperties;
    }

    @Bean
    public OrganisaatioClient organisaatioClient() {
        return new OrganisaatioClientImpl(urlConfiguration, objectMapper, commonProperties);
    }
}
