package fi.vm.sade.cas.oppija.configuration.webflow;

import fi.vm.sade.cas.oppija.configuration.action.SurrogateAuthenticationAction;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import static fi.vm.sade.cas.oppija.surrogate.SurrogateConstants.CODE_PARAMETER_NAME;
import static fi.vm.sade.cas.oppija.surrogate.SurrogateConstants.TOKEN_PARAMETER_NAME;
import static org.apereo.cas.web.flow.CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE;

@Component
public class SurrogateAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String STATE_ID_CODE_PARAMETER_DECISION = "surrogateCodeParamDecision";
    private static final String STATE_ID_TOKEN_PARAMETER_DECISION = "surrogateTokenParamDecision";
    private static final String STATE_ID_SURROGATE_ACTION = "surrogateAuthenticationAction";
    private static final String STATE_ID_SURROGATE_CANCEL = "surrogateAuthenticationCancel";

    private final SurrogateAuthenticationAction surrogateAuthenticationAction;

    public SurrogateAuthenticationWebflowConfigurer(FlowBuilderServices flowBuilderServices,
                                                    @Qualifier("loginFlowRegistry") FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                    ConfigurableApplicationContext applicationContext,
                                                    CasConfigurationProperties casProperties,
                                                    @Lazy
                                                    SurrogateAuthenticationAction surrogateAuthenticationAction) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        this.surrogateAuthenticationAction = surrogateAuthenticationAction;
    }

    @Override
    protected void doInitialize() {
        Flow loginFlow = super.getLoginFlow();
        StateDefinition originalStartState = loginFlow.getStartState();
        /* This is the flow for returning from Valtuuded service.
        Token indicates that its coming from service.
        If token is available and code is not, ITS A CANCEL BUTTON PRESS so it should be transitioned to logout
         */
        String tokenDecision = String.format("requestParameters.%1$s != null && !requestParameters.%1$s.isEmpty()",
                TOKEN_PARAMETER_NAME);
        String codeDecision = String.format("requestParameters.%1$s != null && !requestParameters.%1$s.isEmpty()",
               CODE_PARAMETER_NAME);
        EndState cancelState = super.createEndState(loginFlow, STATE_ID_SURROGATE_CANCEL,
                '\'' + CasWebflowConfigurer.FLOW_ID_LOGOUT + '\'', true);

        /* first check token by STATE_ID_CODE_PARAMETER_DECISION, then check code by STATE_ID_TOKEN_PARAMETER_DECISION
            if no token -> original startState, if token -> STATE_ID_CODE_PARAMETER_DECISION, then if no code -> cancelState,
            if code -> STATE_ID_SURROGATE_ACTION
         */
        DecisionState codeDecisionState = super.createDecisionState(loginFlow, STATE_ID_CODE_PARAMETER_DECISION, codeDecision,
                STATE_ID_SURROGATE_ACTION, cancelState.getId());
        DecisionState tokenDecisionState = super.createDecisionState(loginFlow, STATE_ID_TOKEN_PARAMETER_DECISION, tokenDecision,
                codeDecisionState.getId(), originalStartState.getId());
        loginFlow.setStartState(tokenDecisionState);
        /*
        Create STATE_ID_SURROGATE_ACTION and its transitions so that it will continue cas own flow afterwards.
         */
        ActionState actionState = super.createActionState(loginFlow, STATE_ID_SURROGATE_ACTION, surrogateAuthenticationAction);
        super.createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET);
        super.createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_CANCEL, cancelState.getId());
        super.createStateDefaultTransition(actionState, STATE_ID_HANDLE_AUTHN_FAILURE);
        actionState.getExitActionList().add(super.createEvaluateAction(
                CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS));
    }

}
