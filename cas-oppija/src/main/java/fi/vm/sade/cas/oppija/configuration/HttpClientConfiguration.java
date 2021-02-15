package fi.vm.sade.cas.oppija.configuration;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.auth.CasAuthenticator;
import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;
import fi.vm.sade.properties.OphProperties;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.message.BasicHeader;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.HttpClientProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.net.ssl.HostnameVerifier;
import java.util.Arrays;

@Configuration
public class HttpClientConfiguration {

    private static final String CALLER_ID = "1.2.246.562.10.00000000001.cas-oppija";

    private final CasConfigurationProperties casProperties;
    private final SSLConnectionSocketFactory trustStoreSslSocketFactory;
    private final HostnameVerifier hostnameVerifier;

    public HttpClientConfiguration(CasConfigurationProperties casProperties,
                                   SSLConnectionSocketFactory trustStoreSslSocketFactory,
                                   HostnameVerifier hostnameVerifier) {
        this.casProperties = casProperties;
        this.trustStoreSslSocketFactory = trustStoreSslSocketFactory;
        this.hostnameVerifier = hostnameVerifier;
    }

    @Bean
    public fi.vm.sade.javautils.httpclient.OphHttpClient httpClient() {
        return ApacheOphHttpClient.createDefaultOphClient(CALLER_ID, null);
    }

    @Bean
    public OphHttpClient oppijanumerorekisteriHttpClient(OphProperties properties, Environment environment) {
        CasAuthenticator authenticator = new CasAuthenticator.Builder()
                .username(environment.getRequiredProperty("service-user.username"))
                .password(environment.getRequiredProperty("service-user.password"))
                .webCasUrl(properties.url("cas.base"))
                .casServiceUrl(properties.url("oppijanumerorekisteri-service.security_check"))
                .build();
        return new OphHttpClient.Builder(CALLER_ID).authenticator(authenticator).build();
    }

    // override cas httpclient to include caller-id header
    @Bean
    public HttpClient noRedirectHttpClient() {
        return getHttpClient(false);
    }

    // override cas httpclient to include caller-id header
    @Bean
    public HttpClient supportsTrustStoreSslSocketFactoryHttpClient() {
        return getHttpClient(true);
    }

    // copy from CasCoreHttpConfiguration
    private HttpClient getHttpClient(final boolean redirectEnabled) {
        SimpleHttpClientFactoryBean c = buildHttpClientFactoryBean();
        c.setRedirectsEnabled(redirectEnabled);
        c.setCircularRedirectsAllowed(redirectEnabled);
        c.setSslSocketFactory(trustStoreSslSocketFactory);
        c.setHostnameVerifier(hostnameVerifier);
        return c.getObject();
    }

    // copy from CasCoreHttpConfiguration (added caller-id header)
    private SimpleHttpClientFactoryBean buildHttpClientFactoryBean() {
        SimpleHttpClientFactoryBean.DefaultHttpClient c = new SimpleHttpClientFactoryBean.DefaultHttpClient();
        HttpClientProperties httpClient = casProperties.getHttpClient();
        c.setConnectionTimeout(Beans.newDuration(httpClient.getConnectionTimeout()).toMillis());
        c.setReadTimeout((int) Beans.newDuration(httpClient.getReadTimeout()).toMillis());
        c.setDefaultHeaders(
                Arrays.asList(
                        new BasicHeader("Caller-Id", CALLER_ID),
                        new BasicHeader("CSRF", CALLER_ID)
                )
        );
        c.setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE);
        return c;
    }

}
