package fi.vm.sade.cas.oppija.configuration;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.flow.*;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.pac4j.DelegatedSessionCookieManager;
import org.pac4j.core.client.Clients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This class should include only fixes to default cas delegated authentication configuration.
 */
@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DelegatedAuthenticationConfiguration implements CasWebflowExecutionPlanConfigurer, Ordered {

    private final FlowBuilderServices flowBuilderServices;
    private final FlowDefinitionRegistry loginFlowDefinitionRegistry;
    private final FlowDefinitionRegistry logoutFlowDefinitionRegistry;
    private final Action saml2ClientLogoutAction;
    private final ApplicationContext applicationContext;
    private final CasConfigurationProperties casProperties;
    private final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;
    private final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;
    private final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;
    private final Clients builtClients;
    private final ServicesManager servicesManager;
    private final AuditableExecution registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer;
    private final DelegatedClientWebflowManager delegatedClientWebflowManager;
    private final DelegatedSessionCookieManager delegatedSessionCookieManager;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;
    private final CentralAuthenticationService centralAuthenticationService;

    public DelegatedAuthenticationConfiguration(FlowBuilderServices flowBuilderServices,
                                                @Qualifier("loginFlowRegistry") FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                @Qualifier("logoutFlowRegistry") FlowDefinitionRegistry logoutFlowDefinitionRegistry,
                                                Action saml2ClientLogoutAction,
                                                ApplicationContext applicationContext,
                                                CasConfigurationProperties casProperties,
                                                CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                                CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                                AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                                Clients builtClients,
                                                ServicesManager servicesManager,
                                                AuditableExecution registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer,
                                                DelegatedClientWebflowManager delegatedClientWebflowManager,
                                                DelegatedSessionCookieManager delegatedSessionCookieManager,
                                                AuthenticationSystemSupport authenticationSystemSupport,
                                                AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                                CentralAuthenticationService centralAuthenticationService) {
        this.flowBuilderServices = flowBuilderServices;
        this.loginFlowDefinitionRegistry = loginFlowDefinitionRegistry;
        this.logoutFlowDefinitionRegistry = logoutFlowDefinitionRegistry;
        this.saml2ClientLogoutAction = saml2ClientLogoutAction;
        this.applicationContext = applicationContext;
        this.casProperties = casProperties;
        this.initialAuthenticationAttemptWebflowEventResolver = initialAuthenticationAttemptWebflowEventResolver;
        this.serviceTicketRequestWebflowEventResolver = serviceTicketRequestWebflowEventResolver;
        this.adaptiveAuthenticationPolicy = adaptiveAuthenticationPolicy;
        this.builtClients = builtClients;
        this.servicesManager = servicesManager;
        this.registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer = registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer;
        this.delegatedClientWebflowManager = delegatedClientWebflowManager;
        this.delegatedSessionCookieManager = delegatedSessionCookieManager;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.authenticationRequestServiceSelectionStrategies = authenticationRequestServiceSelectionStrategies;
        this.centralAuthenticationService = centralAuthenticationService;
    }

    @Override
    public void configureWebflowExecutionPlan(CasWebflowExecutionPlan plan) {
        // this is from default delegatedAuthenticationWebflowConfigurer bean:
        plan.registerWebflowConfigurer(new DelegatedAuthenticationWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, logoutFlowDefinitionRegistry, saml2ClientLogoutAction, applicationContext,
                casProperties));

        plan.registerWebflowConfigurer(new AbstractCasWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            protected void doInitialize() {
                // fix delegatedAuthenticationAction success transition
                ActionState realSubmitState = getState(getLoginFlow(), CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
                TransitionDefinition successTransition = realSubmitState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
                String successTargetStateId = successTransition.getTargetStateId();
                TransitionableState state = getState(getLoginFlow(), CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION);
                createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUCCESS, successTargetStateId, true);

                // add delegatedAuthenticationAction cancel transition
                EndState cancelState = super.createEndState(getLoginFlow(), CasWebflowConstants.TRANSITION_ID_CANCEL,
                        '\'' + CasWebflowConfigurer.FLOW_ID_LOGOUT + '\'', true);
                createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_CANCEL, cancelState.getId());
            }
        });
    }

    @Override
    public int getOrder() {
        // This CasWebflowExecutionPlanConfigurer must be run before SurrogateConfiguration to able to cancel auth
        // but after InterruptConfiguration to enable surrogate authentication after delegated authentication
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    // override default delegatedAuthenticationWebflowConfigurer to be able to override its flow definitions (see above)
    @Bean
    public CasWebflowConfigurer delegatedAuthenticationWebflowConfigurer() {
        return new AbstractCasWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            protected void doInitialize() {
                // nop
            }
        };
    }

    // override default delegatedAuthenticationAction to automatically logout on error
    @Bean
    public Action delegatedAuthenticationAction() {
        return new DelegatedClientAuthenticationAction(
                initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy,
                builtClients,
                servicesManager,
                registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer,
                delegatedClientWebflowManager,
                delegatedSessionCookieManager,
                authenticationSystemSupport,
                casProperties.getLocale().getParamName(),
                casProperties.getTheme().getParamName(),
                authenticationRequestServiceSelectionStrategies,
                centralAuthenticationService) {
            @Override
            public Event doExecute(RequestContext context) {
                try {
                    return super.doExecute(context);
                } catch (Exception e) {
                    return result(CasWebflowConstants.TRANSITION_ID_CANCEL);
                }
            }
        };
    }

}
