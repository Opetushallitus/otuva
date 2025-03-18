package fi.vm.sade.auth.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import fi.vm.sade.saml.action.SAMLAction;
import lombok.val;

public class WebflowConfiguration extends AbstractCasWebflowConfigurer {
    private static final String STATE_ID_SAML_DECISION = "samlDecision";
    private static final String STATE_ID_SAML_ACTION = "samlAction";
    public static final String MFA_GAUTH_EVENT_ID = "mfa-gauth";

    public WebflowConfiguration(
        final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getAuthn().getPac4j().getWebflow().getOrder() + 1);
    }

    @Override
    protected void doInitialize() {
        val flowDefn = getLoginFlow();
        configureAuthTokenLoginFlow(flowDefn);
        configureDelegatedAuthenticationLoginFlow(flowDefn);
    }

    private void configureAuthTokenLoginFlow(Flow loginFlow) {
        StateDefinition originalStartState = loginFlow.getStartState();

        String decision = "requestParameters.authToken != null && !requestParameters.authToken.isEmpty()";
        DecisionState decisionState = super.createDecisionState(loginFlow, STATE_ID_SAML_DECISION, decision,
                STATE_ID_SAML_ACTION, originalStartState.getId());
        loginFlow.setStartState(decisionState);

        ActionState actionState = super.createActionState(loginFlow, STATE_ID_SAML_ACTION, SAMLAction.BEAN_NAME);
        super.createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET);
        super.createTransitionForState(actionState, MFA_GAUTH_EVENT_ID, MFA_GAUTH_EVENT_ID);

        super.createStateDefaultTransition(actionState, originalStartState);
        actionState.getExitActionList().add(super.createEvaluateAction(
                CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS));
    }

    private void configureDelegatedAuthenticationLoginFlow(Flow loginFlow) {
        TransitionableState delegatedAuthenticationState = getState(loginFlow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION);

        // Delegated authentication (Haka/Mpassid) should go through mfa
        createTransitionForState(delegatedAuthenticationState, CasWebflowConstants.TRANSITION_ID_SUCCESS, MFA_GAUTH_EVENT_ID, true);

        // Redirect back to login form on error
        createTransitionForState(delegatedAuthenticationState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, true);
    }
}
