package fi.vm.sade;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;

import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlanConfigurer;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.webflow.execution.Action;

import fi.vm.sade.auth.cas.DelegatedIdpRedirectionStrategy;
import fi.vm.sade.auth.cas.OtuvaDelegatedAuthenticationProcessor;
import fi.vm.sade.auth.clients.HttpClientUtil;
import fi.vm.sade.auth.clients.KayttooikeusClient;
import fi.vm.sade.auth.clients.OppijanumerorekisteriClient;
import fi.vm.sade.auth.interrupt.LoginRedirectInterruptInquirer;
import fi.vm.sade.auth.interrupt.LoginRedirectUrlGenerator;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient;
import fi.vm.sade.properties.OphProperties;

@Configuration
@ComponentScan
@RequiredArgsConstructor
public class CasOphConfiguration {
    final PrincipalFactory principalFactory;
    final Environment environment;

    @Value("${registration.enabled}")
    private boolean registrationEnabled;

    @Bean
    OphHttpClient httpClient(OphProperties ophProperties) {
        return ApacheOphHttpClient.createDefaultOphClient(HttpClientUtil.CALLER_ID, ophProperties);
    }

    @Bean
    ObservationRegistry observationRegistry() {
        // Disable all observations
        return ObservationRegistry.NOOP;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    DelegatedAuthenticationPreProcessor delegatedAuthenticationProcessor(KayttooikeusClient kayttooikeusClient) {
        return new OtuvaDelegatedAuthenticationProcessor(principalFactory, kayttooikeusClient, registrationEnabled);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy() {
        return new DelegatedIdpRedirectionStrategy();
    }

    @Bean
    TicketSerializationExecutionPlanConfigurer ticketSerializationExecutionPlanConfigurer() {
        return plan -> {
            plan.registerTicketSerializer(new OtuvaTransientSessionTicketSerializer());
        };
    }

    @Bean
    InterruptInquirer loginRedirectInterruptInquirer(KayttooikeusClient kayttooikeusClient, OppijanumerorekisteriClient oppijanumerorekisteriClient, OphProperties ophProperties) {
        return new LoginRedirectInterruptInquirer(
                kayttooikeusClient,
                new LoginRedirectUrlGenerator(kayttooikeusClient, oppijanumerorekisteriClient, ophProperties)
        );
    }

    @Bean
    InterruptInquiryExecutionPlanConfigurer interruptInquiryExecutionPlanConfigurer(
            @Qualifier("loginRedirectInterruptInquirer") InterruptInquirer loginRedirectInterruptInquirer) {
        return plan -> {
            plan.registerInterruptInquirer(loginRedirectInterruptInquirer);
        };
    }

    @Bean
    TomcatGate tomcatGate() {
        return new TomcatGate();
    }

    @Bean(name = "otuvaJpaTicketRegistry")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    OtuvaJpaTicketRegistry otuvaJpaTicketRegistry(
            @Qualifier(TicketSerializationManager.BEAN_NAME)
            final TicketSerializationManager ticketSerializationManager,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("jpaTicketRegistryTransactionTemplate")
            final TransactionOperations jpaTicketRegistryTransactionTemplate,
            @Qualifier(TicketCatalog.BEAN_NAME)
            final TicketCatalog ticketCatalog,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory) {
        var jpa = casProperties.getTicket().getRegistry().getJpa();
        var cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(jpa.getCrypto(), "jpa");
        return new OtuvaJpaTicketRegistry(cipher, ticketSerializationManager, ticketCatalog, applicationContext,
            jpaBeanFactory, jpaTicketRegistryTransactionTemplate, casProperties);
    }

    @Bean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_LOGOUT)
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    Action delegatedSaml2ClientLogoutAction(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("otuvaJpaTicketRegistry")
        final OtuvaJpaTicketRegistry otuvaJpaTicketRegistry,
        @Qualifier(SingleLogoutRequestExecutor.BEAN_NAME)
        final SingleLogoutRequestExecutor singleLogoutRequestExecutor) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new OtuvaDelegatedSaml2ClientLogoutAction(otuvaJpaTicketRegistry, singleLogoutRequestExecutor))
            .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_CLIENT_LOGOUT)
            .build()
            .get();
    }
}
