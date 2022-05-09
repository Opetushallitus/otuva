package fi.vm.sade.cas.oppija.configuration;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.auth.CasAuthenticator;
import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;
import fi.vm.sade.properties.OphProperties;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.HttpClientProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.net.ssl.HostnameVerifier;
import java.util.ArrayList;
import java.util.Arrays;

@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CallerIdHttpClientConfiguration {

    private static final String CALLER_ID = "1.2.246.562.10.00000000001.cas-oppija";

    private final CasConfigurationProperties casProperties;
    private final CasSSLContext casSslContext;
    private final HostnameVerifier hostnameVerifier;
    private final LayeredConnectionSocketFactory trustStoreSslSocketFactory;

    public CallerIdHttpClientConfiguration(CasConfigurationProperties casProperties,
                                           CasSSLContext casSslContext,
                                           HostnameVerifier hostnameVerifier,
                                           LayeredConnectionSocketFactory trustStoreSslSocketFactory) {
        this.casProperties = casProperties;
        this.casSslContext = casSslContext;
        this.hostnameVerifier = hostnameVerifier;
        this.trustStoreSslSocketFactory = trustStoreSslSocketFactory;
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
        return c.getObject();
    }

    // copy from CasCoreHttpConfiguration (added caller-id header)
    private SimpleHttpClientFactoryBean buildHttpClientFactoryBean() {
        SimpleHttpClientFactoryBean.DefaultHttpClient c = new SimpleHttpClientFactoryBean.DefaultHttpClient();
        HttpClientProperties httpClient = casProperties.getHttpClient();
        c.setConnectionTimeout(Beans.newDuration(httpClient.getConnectionTimeout()).toMillis());
        c.setReadTimeout((int) Beans.newDuration(httpClient.getReadTimeout()).toMillis());

        if (StringUtils.isNotBlank(httpClient.getProxyHost()) && httpClient.getProxyPort() > 0) {
            c.setProxy(new HttpHost(httpClient.getProxyHost(), httpClient.getProxyPort()));
        }
        c.setSslSocketFactory(trustStoreSslSocketFactory);
        c.setHostnameVerifier(hostnameVerifier);
        c.setSslContext(casSslContext.getSslContext());
        c.setTrustManagers(casSslContext.getTrustManagers());
        val defaultHeaders = new ArrayList<Header>();
        httpClient.getDefaultHeaders().forEach((name, value) -> defaultHeaders.add(new BasicHeader(name, value)));
        defaultHeaders.add(new BasicHeader("Caller-Id", CALLER_ID));
        defaultHeaders.add(new BasicHeader("CSRF", CALLER_ID));
        c.setDefaultHeaders(defaultHeaders);
        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(new BasicClientCookie("CSRF", CALLER_ID));
        c.setCookieStore(cookieStore);
        c.setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE);
        return c;
    }

}
