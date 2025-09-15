package fi.vm.sade;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;

import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.webflow.execution.Action;

import fi.vm.sade.auth.cas.DelegatedIdpRedirectionStrategy;
import fi.vm.sade.auth.cas.OtuvaDelegatedAuthenticationProcessor;
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
        return new OtuvaDelegatedAuthenticationProcessor(principalFactory, kayttooikeusRestClient());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy() {
        return new DelegatedIdpRedirectionStrategy();
    }

    @Bean
    public TicketSerializationExecutionPlanConfigurer ticketSerializationExecutionPlanConfigurer() {
        return plan -> {
            plan.registerTicketSerializer(new OtuvaTransientSessionTicketSerializer());
        };
    }

    @Bean
    TomcatGate tomcatGate() {
        return new TomcatGate();
    }

    @Bean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_LOGOUT)
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action delegatedSaml2ClientLogoutAction(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry,
        @Qualifier(SingleLogoutRequestExecutor.BEAN_NAME)
        final SingleLogoutRequestExecutor singleLogoutRequestExecutor) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new FixedDelegatedSaml2ClientLogoutAction(ticketRegistry, singleLogoutRequestExecutor))
            .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_LOGOUT)
            .build()
            .get();
    }
}
