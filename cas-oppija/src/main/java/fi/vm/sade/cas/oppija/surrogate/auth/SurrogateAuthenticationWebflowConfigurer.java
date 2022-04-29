package fi.vm.sade.cas.oppija.surrogate.auth;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import static fi.vm.sade.cas.oppija.surrogate.SurrogateConstants.TOKEN_PARAMETER_NAME;
import static org.apereo.cas.web.flow.CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE;

@Component
public class SurrogateAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String STATE_ID_SURROGATE_DECISION = "surrogateAuthenticationDecision";
    private static final String STATE_ID_SURROGATE_ACTION = "surrogateAuthenticationAction";
    private static final String STATE_ID_SURROGATE_CANCEL = "surrogateAuthenticationCancel";

    private final SurrogateAuthenticationAction surrogateAuthenticationAction;

    public SurrogateAuthenticationWebflowConfigurer(FlowBuilderServices flowBuilderServices,
                                                    @Qualifier("loginFlowRegistry") FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                    ConfigurableApplicationContext applicationContext,
                                                    CasConfigurationProperties casProperties,
                                                    SurrogateAuthenticationAction surrogateAuthenticationAction) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        this.surrogateAuthenticationAction = surrogateAuthenticationAction;
    }

    @Override
    protected void doInitialize() {
        Flow loginFlow = super.getLoginFlow();
        StateDefinition originalStartState = loginFlow.getStartState();

        String decision = String.format("requestParameters.%1$s != null && !requestParameters.%1$s.isEmpty()",
                TOKEN_PARAMETER_NAME);
        DecisionState decisionState = super.createDecisionState(loginFlow, STATE_ID_SURROGATE_DECISION, decision,
                STATE_ID_SURROGATE_ACTION, originalStartState.getId());
        loginFlow.setStartState(decisionState);

        ActionState actionState = super.createActionState(loginFlow, STATE_ID_SURROGATE_ACTION, surrogateAuthenticationAction);
        super.createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET);
        EndState cancelState = super.createEndState(loginFlow, STATE_ID_SURROGATE_CANCEL,
                '\'' + CasWebflowConfigurer.FLOW_ID_LOGOUT + '\'', true);
        super.createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_CANCEL, cancelState.getId());
        super.createStateDefaultTransition(actionState, STATE_ID_HANDLE_AUTHN_FAILURE);
        actionState.getExitActionList().add(super.createEvaluateAction(
                CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS));
    }

}
