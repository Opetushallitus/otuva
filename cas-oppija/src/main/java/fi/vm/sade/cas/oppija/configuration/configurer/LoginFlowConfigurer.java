package fi.vm.sade.cas.oppija.configuration.configurer;

import fi.vm.sade.cas.oppija.configuration.action.Pac4jClientProvider;
import fi.vm.sade.cas.oppija.configuration.action.SamlLogoutExecuteAction;
import fi.vm.sade.cas.oppija.configuration.action.SamlLogoutPrepareAction;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedAuthenticationWebflowConfigurer;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;
import org.pac4j.core.client.Clients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.*;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.apereo.cas.web.flow.CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION;

public class LoginFlowConfigurer extends DelegatedAuthenticationWebflowConfigurer {
    @Autowired
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    private static final String TRANSITION_ID_LOGOUT = "logout";

    private final TicketRegistrySupport ticketRegistrySupport;
    private final Clients builtClients;

    public LoginFlowConfigurer(final FlowBuilderServices flowBuilderServices,
                               final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                               final FlowDefinitionRegistry logoutFlowDefinitionRegistry,
                               final ConfigurableApplicationContext applicationContext,
                               final CasConfigurationProperties casProperties,
                               final TicketRegistrySupport ticketRegistrySupport,
                               final Clients builtClients

    ) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, logoutFlowDefinitionRegistry, applicationContext, casProperties);
        this.ticketRegistrySupport = ticketRegistrySupport;
        this.builtClients = builtClients;
    }

    @Override
    protected void doInitialize() {
        final Flow loginFlow = super.getLoginFlow();
        final Flow logoutFlow = super.getLogoutFlow();
        if (loginFlow != null) {
            createClientActionState(loginFlow);
            createStopWebflowViewState(loginFlow);
            /*createSaml2ClientLogoutAction();*/
        }
        /*String f = loginFlow.toString();
        if(super.containsFlowState(loginFlow, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE)) {
            ActionState state = super.getState(loginFlow, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE, ActionState.class);
            ActionList entryActionList = state.getEntryActionList();
            // Transition t = super.createTransitionForState(state, "PreventedError", "hopophop");
            //ts.toArray()[9].setExecutionCriteria();
            TransitionSet ts = state.getTransitionSet();
            f = f + "1";
            //state.add({ requestContext ->
             //       def flowScope = requestContext.flowScope
             //       def httpRequest = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext)

              //      logger.info("Action executing as part of ${state.id}. Stuff happens...")
           // return new Event(this, "success")
        }*/
        // Magic happens; Call 'super' to see what you have access to...

        // fix delegatedAuthenticationAction success transition
        ActionState realSubmitState = super.getState(loginFlow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
        TransitionDefinition successTransition = realSubmitState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        String successTargetStateId = successTransition.getTargetStateId();
        TransitionableState state = super.getTransitionableState(loginFlow, STATE_ID_DELEGATED_AUTHENTICATION);
        super.createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUCCESS, successTargetStateId, true);

        // add delegatedAuthenticationAction cancel transition
        EndState cancelState = super.createEndState(loginFlow, CasWebflowConstants.TRANSITION_ID_CANCEL,
                '\'' + CasWebflowConfigurer.FLOW_ID_LOGOUT + '\'', true);
        super.createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_CANCEL, cancelState.getId());

        // add delegatedAuthenticationAction logout transition
        super.createTransitionForState(state, TRANSITION_ID_LOGOUT, CasWebflowConstants.STATE_ID_TERMINATE_SESSION);

        // add saml service provider initiated logout support
        super.setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry);
        TransitionableState startState = getStartState(logoutFlow);
        ActionState singleLogoutPrepareAction = createActionState(logoutFlow, "samlLogoutPrepareAction",
                new SamlLogoutPrepareAction(ticketGrantingTicketCookieGenerator, ticketRegistrySupport));
        createStateDefaultTransition(singleLogoutPrepareAction, startState.getId());
        setStartState(logoutFlow, singleLogoutPrepareAction);
        DecisionState finishLogoutState = getState(logoutFlow, CasWebflowConstants.STATE_ID_FINISH_LOGOUT, DecisionState.class);
        ActionList entryActionList = finishLogoutState.getEntryActionList();
        clear(entryActionList, entryActionList::remove);
        var clientProvider = new Pac4jClientProvider(builtClients);
        entryActionList.add(new SamlLogoutExecuteAction(clientProvider));


    }

    private static <E, T extends Iterable<E>> void clear(T iterable, Consumer<E> remover) {
        StreamSupport.stream(iterable.spliterator(), false).collect(toList()).forEach(remover::accept);
    }
}