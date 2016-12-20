package fi.vm.sade.kayttooikeus.service.external.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.kayttooikeus.config.properties.UrlConfiguration;
import fi.vm.sade.kayttooikeus.service.external.ExternalServiceException;
import fi.vm.sade.kayttooikeus.service.external.KoodistoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class KoodistoClientImpl implements KoodistoClient {
    private final CachingRestClient restClient = new CachingRestClient()
            .setClientSubSystemCode("kayttooikeus.kayttooikeuspalvelu-service");
    private final UrlConfiguration urlConfiguration;
    private final ObjectMapper objectMapper;

    @Autowired
    public KoodistoClientImpl(UrlConfiguration urlConfiguration, ObjectMapper objectMapper) {
        this.urlConfiguration = urlConfiguration;
        this.objectMapper = objectMapper;
    }

    public List<KoodiArvoDto> listKoodisto(String koodisto) {
        String url = urlConfiguration.url("koodisto-service.koodiston.koodit", koodisto);
        try {
            return objectMapper.readerFor(new TypeReference<List<KoodiArvoDto>>() {})
                    .readValue(restClient.getAsString(url));
        } catch (IOException e) {
            throw new ExternalServiceException(url, "Could not list koodisto " + koodisto + ": " + e.getMessage(), e);
        }
    }
}
