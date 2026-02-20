package fi.vm.sade;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.webflow.execution.Action;

import fi.vm.sade.cas.oppija.controller.UserController;
import fi.vm.sade.client.OppijanumerorekisteriClient;

@Configuration
@ComponentScan
@RequiredArgsConstructor
public class OtuvaCasConfiguration {
    final PrincipalFactory principalFactory;
    final OppijanumerorekisteriClient oppijanumerorekisteriClient;
    final Environment environment;

    @Bean
    ObservationRegistry observationRegistry() {
        // Disable all observations
        return ObservationRegistry.NOOP;
    }

    @Bean
    TicketSerializationExecutionPlanConfigurer ticketSerializationExecutionPlanConfigurer() {
        return plan -> {
            plan.registerTicketSerializer(new OtuvaTransientSessionTicketSerializer());
        };
    }

    @Bean
    TomcatGate tomcatGate() {
        return new TomcatGate();
    }

    @Bean
    DelegatedAuthenticationPreProcessor otuvaDelegatedAuthenticationPreProcessor() {
        return new OtuvaDelegatedAuthenticationPreProcessor(principalFactory, oppijanumerorekisteriClient);
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

    @Bean(name = "otuvaUserController")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    UserController otuvaUserController(
            @Qualifier("ticketGrantingTicketCookieGenerator") CasCookieBuilder casCookieBuilder,
            TicketRegistry ticketRegistry,
            ArgumentExtractor argumentExtractor,
            ServicesManager servicesManager,
            ApplicationContext applicationContext) {
        return new UserController(casCookieBuilder, ticketRegistry, argumentExtractor, servicesManager, applicationContext);
    }

    @Bean
    @ConditionalOnProperty("user-controller.enabled")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    CasWebSecurityConfigurer<Void> otuvaUserControllerSecurityConfigurer() {
        return new CasWebSecurityConfigurer<>() {
            @Override
            public List<String> getIgnoredEndpoints() {
                return List.of("/user");
            }
        };
    }
}
