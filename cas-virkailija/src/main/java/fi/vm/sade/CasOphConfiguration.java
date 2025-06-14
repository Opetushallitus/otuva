package fi.vm.sade;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import fi.vm.sade.auth.cas.DelegatedAuthenticationProcessor;
import fi.vm.sade.auth.cas.DelegatedIdpRedirectionStrategy;
import fi.vm.sade.auth.clients.HttpClientUtil;
import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.auth.discovery.SamlClientAuthenticationRequestCustomizer;
import fi.vm.sade.auth.discovery.SamlDiscoveryAction;
import fi.vm.sade.auth.discovery.SamlDiscoveryFinalizerAction;
import fi.vm.sade.auth.discovery.SamlDiscoveryReturnController;
import fi.vm.sade.auth.discovery.SamlDiscoveryWebflowConfigurer;
import fi.vm.sade.auth.discovery.SamlDiscoveryWebflowConstants;
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
    CasWebflowConfigurer samlDiscoveryWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier("delegatedClientRedirectFlowRegistry")
            final FlowDefinitionRegistry delegatedClientRedirectFlowRegistry,
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
            final DelegatedClientAuthenticationConfigurationContext configContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
        val cfg = new SamlDiscoveryWebflowConfigurer(flowBuilderServices, flowDefinitionRegistry,
                delegatedClientRedirectFlowRegistry, configContext, applicationContext, casProperties);
        cfg.setOrder(casProperties.getAuthn().getPac4j().getWebflow().getOrder() + 100);
        return cfg;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    Action delegatedAuthenticationDiscoveryAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
            final DelegatedClientAuthenticationConfigurationContext configContext) {
        return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new SamlDiscoveryAction(casProperties, configContext))
                .withId(SamlDiscoveryWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_DISCOVERY)
                .build()
                .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    Action delegatedAuthenticationFinalizeDiscoveryAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
            final DelegatedClientAuthenticationConfigurationContext configContext) {
        return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new SamlDiscoveryFinalizerAction(casProperties, configContext))
                .withId(SamlDiscoveryWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_FINALIZE_DISCOVERY)
                .build()
                .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DelegatedClientAuthenticationRequestCustomizer discoveryRequestCustomizer(
            final CasConfigurationProperties casProperties) {
        return new SamlClientAuthenticationRequestCustomizer(casProperties);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "samlDiscoveryReturnController")
    SamlDiscoveryReturnController samlDiscoveryReturnController(
            final CasConfigurationProperties casProperties,
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
            final DelegatedClientAuthenticationConfigurationContext configContext) {
        return new SamlDiscoveryReturnController(casProperties, configContext);
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
}
