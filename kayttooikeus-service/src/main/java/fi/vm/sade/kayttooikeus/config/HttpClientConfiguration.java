package fi.vm.sade.kayttooikeus.config;

import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class HttpClientConfiguration {
    public static final String CALLER_ID = "1.2.246.562.10.00000000001.kayttooikeus-service.backend";

    @Bean
    @Primary
    public OphHttpClient httpClient() {
        return ApacheOphHttpClient.createDefaultOphClient(CALLER_ID, null);
    }
}
