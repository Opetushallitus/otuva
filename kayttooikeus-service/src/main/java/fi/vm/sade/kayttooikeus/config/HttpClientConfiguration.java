package fi.vm.sade.kayttooikeus.config;

import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;
import fi.vm.sade.javautils.http.auth.CasAuthenticator;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.kayttooikeus.config.properties.UrlConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class HttpClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientConfiguration.class);
    public static final String CALLER_ID = "1.2.246.562.10.00000000001.kayttooikeus-service.backend";

    public static final String DEFAULT_TIMEOUT = String.valueOf(10000);
    public static final String HTTP_CLIENT_OPPIJANUMEROREKISTERI = "httpClientOppijanumerorekisteri";
    public static final String HTTP_CLIENT_ORGANISAATIO = "httpClientOrganisaatio";
    public static final String HTTP_CLIENT_VIESTINTA = "httpClientViestinta";

    @Value("${palvelukayttaja.username}")
    private String username;
    @Value("${palvelukayttaja.password}")
    private String password;

    @Bean
    @Primary
    public OphHttpClient httpClient() {
        return ApacheOphHttpClient.createDefaultOphClient(CALLER_ID, null);
    }


    @Bean(HTTP_CLIENT_OPPIJANUMEROREKISTERI)
    public fi.vm.sade.javautils.http.OphHttpClient httpClientOppijanumerorekisteri(UrlConfiguration properties) {
        CasAuthenticator authenticator = new CasAuthenticator.Builder()
                .username(username)
                .password(password)
                .webCasUrl(properties.url("cas.url"))
                .casServiceUrl(properties.url("oppijanumerorekisteri-service.security-check"))
                .build();
        return new fi.vm.sade.javautils.http.OphHttpClient.Builder(CALLER_ID).authenticator(authenticator).build();
    }

    @Bean(HTTP_CLIENT_ORGANISAATIO)
    public fi.vm.sade.javautils.http.OphHttpClient httpClientOrganisaatio(UrlConfiguration properties) {
        int timeout = Integer.parseInt(properties.getOrElse("organisaatio-service.timeout", DEFAULT_TIMEOUT));
        LOGGER.info("Organisaatio HTTP client timeout: {} ms", timeout);
        CasAuthenticator authenticator = new CasAuthenticator.Builder()
                .username(username)
                .password(password)
                .webCasUrl(properties.url("cas.url"))
                .casServiceUrl(properties.url("organisaatio-service.security-check"))
                .build();
        return new fi.vm.sade.javautils.http.OphHttpClient.Builder(CALLER_ID)
                .timeoutMs(timeout)
                .setSocketTimeoutMs(timeout)
                .authenticator(authenticator)
                .build();
    }
}
