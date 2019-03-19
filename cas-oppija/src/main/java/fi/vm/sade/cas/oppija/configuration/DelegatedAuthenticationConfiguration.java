package fi.vm.sade.cas.oppija.configuration;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.*;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This class should include only fixes to default cas delegated authentication configuration.
 */
@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DelegatedAuthenticationConfiguration implements CasWebflowExecutionPlanConfigurer {

    private final FlowBuilderServices flowBuilderServices;
    private final FlowDefinitionRegistry loginFlowDefinitionRegistry;
    private final FlowDefinitionRegistry logoutFlowDefinitionRegistry;
    private final Action saml2ClientLogoutAction;
    private final ApplicationContext applicationContext;
    private final CasConfigurationProperties casProperties;

    public DelegatedAuthenticationConfiguration(FlowBuilderServices flowBuilderServices,
                                                @Qualifier("loginFlowRegistry") FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                @Qualifier("logoutFlowRegistry") FlowDefinitionRegistry logoutFlowDefinitionRegistry,
                                                Action saml2ClientLogoutAction,
                                                ApplicationContext applicationContext,
                                                CasConfigurationProperties casProperties) {
        this.flowBuilderServices = flowBuilderServices;
        this.loginFlowDefinitionRegistry = loginFlowDefinitionRegistry;
        this.logoutFlowDefinitionRegistry = logoutFlowDefinitionRegistry;
        this.saml2ClientLogoutAction = saml2ClientLogoutAction;
        this.applicationContext = applicationContext;
        this.casProperties = casProperties;
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
            }
        });
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

}
