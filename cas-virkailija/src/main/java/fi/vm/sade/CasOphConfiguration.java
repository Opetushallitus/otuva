package fi.vm.sade;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;

import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.JpaTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.transaction.support.TransactionOperations;

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

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketRegistry ticketRegistry(
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
        return BeanSupplier.of(TicketRegistry.class)
            .when(true)
            .supply(() -> {
                var jpa = casProperties.getTicket().getRegistry().getJpa();
                var cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(jpa.getCrypto(), "jpa");
                return new JpaTicketRegistry(cipher, ticketSerializationManager, ticketCatalog, applicationContext,
                    jpaBeanFactory, jpaTicketRegistryTransactionTemplate, casProperties);
            })
            .otherwiseProxy()
            .get();
    }
}
