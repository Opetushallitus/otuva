package fi.vm.sade;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.saml2.DelegatedClientSaml2Builder;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;

import fi.vm.sade.auth.cas.DelegatedAuthenticationProcessor;
import fi.vm.sade.auth.cas.DelegatedIdpRedirectionStrategy;
import fi.vm.sade.auth.clients.HttpClientUtil;
import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;

@Configuration
@ComponentScan
@RequiredArgsConstructor
public class CasOphConfiguration {
    final PrincipalFactory principalFactory;
    final Environment environment;

    @Bean
    public CasOphProperties casOphProperties() {
        return new CasOphProperties(environment);
    }

    @Bean
    public OphHttpClient httpClient() {
        return ApacheOphHttpClient.createDefaultOphClient(HttpClientUtil.CALLER_ID, casOphProperties());
    }

    @Bean
    public KayttooikeusRestClient kayttooikeusRestClient() {
        return new KayttooikeusRestClient(casOphProperties(), environment);
    }

    @Bean
    public ObservationRegistry observationRegistry() {
        // Disable all observations
        return ObservationRegistry.NOOP;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DelegatedAuthenticationPreProcessor delegatedAuthenticationProcessor() {
        return new DelegatedAuthenticationProcessor(principalFactory, kayttooikeusRestClient());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy() {
        return new DelegatedIdpRedirectionStrategy();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    CasWebSecurityConfigurer<Void> samlDiscoveryReturnControllerWebSecurityConfigurer() {
        return new CasWebSecurityConfigurer<>() {
            @Override
            public List<String> getIgnoredEndpoints() {
                return List.of("/discovery");
            }
        };
    }

    @Bean
    TomcatGate tomcatGate() {
        return new TomcatGate();
    }

    @Bean(name = "delegatedSaml2ClientBuilder")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DelegatedClientSaml2Builder delegatedSaml2ClientBuilder(
            @Qualifier(DelegatedIdentityProviderFactory.BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY)
            final ObjectProvider<SAMLMessageStoreFactory> samlMessageStoreFactory,
            @Qualifier(CasSSLContext.BEAN_NAME)
            final CasSSLContext casSslContext) {
        return new HakaAwareDelegatedClientSaml2Builder(casSslContext, samlMessageStoreFactory);
    }
}
