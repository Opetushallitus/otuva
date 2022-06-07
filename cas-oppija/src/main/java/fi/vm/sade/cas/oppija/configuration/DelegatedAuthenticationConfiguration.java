package fi.vm.sade.cas.oppija.configuration;

import fi.vm.sade.cas.oppija.CasOppijaConstants;
import fi.vm.sade.cas.oppija.configuration.action.*;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.*;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.session.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionList;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.apereo.cas.web.flow.CasWebflowConstants.TRANSITION_ID_SUCCESS;


/**
 * This class should include only fixes to default cas delegated authentication configuration.
 */
@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DelegatedAuthenticationConfiguration implements CasWebflowExecutionPlanConfigurer, Ordered {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatedAuthenticationConfiguration.class);
    private final FlowBuilderServices flowBuilderServices;
    private final FlowDefinitionRegistry loginFlowDefinitionRegistry;
    private final FlowDefinitionRegistry logoutFlowDefinitionRegistry;
    private final ConfigurableApplicationContext applicationContext;
    private final CasConfigurationProperties casProperties;
    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private final TicketRegistrySupport ticketRegistrySupport;
    private final Clients clients;
    private final SessionStore sessionStore;

    public DelegatedAuthenticationConfiguration(FlowBuilderServices flowBuilderServices,
                                                @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY) FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                @Qualifier(CasWebflowConstants.BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY) FlowDefinitionRegistry logoutFlowDefinitionRegistry,
                                                ConfigurableApplicationContext applicationContext,
                                                CasConfigurationProperties casProperties,
                                                @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER) CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                                TicketRegistrySupport ticketRegistrySupport,
                                                Clients clients,
                                                SessionStore sessionStore) {
        this.flowBuilderServices = flowBuilderServices;
        this.loginFlowDefinitionRegistry = loginFlowDefinitionRegistry;
        this.logoutFlowDefinitionRegistry = logoutFlowDefinitionRegistry;
        this.applicationContext = applicationContext;
        this.casProperties = casProperties;
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.ticketRegistrySupport = ticketRegistrySupport;
        this.clients = clients;
        this.sessionStore = sessionStore;
    }

    /*@Bean
    public CasWebflowConfigurer delegatedAuthenticationWebflowConfigurer() {
        return new DelegatedAuthenticationWebflowConfigurer(
                flowBuilderServices, loginFlowDefinitionRegistry,
                logoutFlowDefinitionRegistry, applicationContext, casProperties
        )
        {

            @Override
            public int getOrder() {
                // This CasWebflowExecutionPlanConfigurer must be run before SurrogateConfiguration to able to cancel auth
                // but after InterruptConfiguration to enable surrogate authentication after delegated authentication
                return Ordered.HIGHEST_PRECEDENCE + 1;
            }
        };
    }*/

    @Bean
    Pac4jClientProvider clientProvider() {
        return new Pac4jClientProvider(clients);
    }

    @Override
    public void configureWebflowExecutionPlan(CasWebflowExecutionPlan plan) {

        LOGGER.debug("default delegateweb flow configured");
        plan.registerWebflowConfigurer(new AbstractCasWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            protected void doInitialize() {
                // Initial login action to collect url parameters: valtuudet & services
                ActionList startActionList = getLoginFlow().getStartActionList();
                startActionList.add(new SamlLoginPrepareAction(getLoginFlow()));
                LOGGER.trace("configuring additional web flow, url parameters collected");


                // fix delegatedAuthenticationAction success transition
                ActionState realSubmitState = getState(getLoginFlow(), CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
                TransitionDefinition successTransition = realSubmitState.getTransition(TRANSITION_ID_SUCCESS);
                String successTargetStateId = successTransition.getTargetStateId();
                LOGGER.trace("ActionState {} has transition {} with target state: {} ", CasWebflowConstants.STATE_ID_REAL_SUBMIT, TRANSITION_ID_SUCCESS, successTargetStateId );
                TransitionableState state = getState(getLoginFlow(), CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION);
                createTransitionForState(state, TRANSITION_ID_SUCCESS, successTargetStateId, true);

                // add delegatedAuthenticationAction cancel transition
                EndState cancelState = super.createEndState(getLoginFlow(), CasWebflowConstants.TRANSITION_ID_CANCEL,
                        '\'' + CasWebflowConfigurer.FLOW_ID_LOGOUT + '\'', true);
                TransitionableState delegatedAuthenticationState = getState(getLoginFlow(), CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION);
                createTransitionForState(delegatedAuthenticationState, CasWebflowConstants.TRANSITION_ID_CANCEL, cancelState.getId());
                // add delegatedAuthenticationAction logout from idp (Suomifi) redirect to login flow
                ActionState IdpLogoutActionState = createActionState(getLoginFlow(), CasOppijaConstants.STATE_ID_IDP_LOGOUT, CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_FINISH_LOGOUT);
                createTransitionForState(delegatedAuthenticationState, CasOppijaConstants.TRANSITION_ID_IDP_LOGOUT, IdpLogoutActionState.getId());
                createStateDefaultTransition(IdpLogoutActionState, CasWebflowConstants.TRANSITION_ID_REDIRECT);

                //EndState returnFromIpdLogoutState = super.createEndState(getLoginFlow(), TRANSITION_ID_LOGOUT,
                //        '\'' + CasWebflowConfigurer.FLOW_ID_LOGOUT + '\'', true);
                //createTransitionForState(delegatedAuthenticationState, TRANSITION_ID_LOGOUT, returnFromIpdLogoutState.getId());
                LOGGER.trace("configuring additional web flow, delegatedAuthenticationAction cancel transition target is now:{}", cancelState.getId());
                // add delegatedAuthenticationAction logout transition
                LOGGER.trace("configuring additional web flow, delegatedAuthenticationAction logout transition added");
                // add saml service provider initiated logout support
                setLogoutFlowDefinitionRegistry(DelegatedAuthenticationConfiguration.this.logoutFlowDefinitionRegistry);
                // logout flow begins
                TransitionableState startState = getStartState(getLogoutFlow());
                ActionState singleLogoutPrepareAction = createActionState(getLogoutFlow(), "samlLogoutPrepareAction",
                        new SamlLogoutPrepareAction(ticketGrantingTicketCookieGenerator, ticketRegistrySupport));
                createStateDefaultTransition(singleLogoutPrepareAction, startState.getId());
                setStartState(getLogoutFlow(), singleLogoutPrepareAction);
                LOGGER.trace("configuring additional web flow, delegatedAuthenticationAction saml-initiated logout support");
                TransitionableState finishLogoutState = getState(getLogoutFlow(), CasWebflowConstants.STATE_ID_FINISH_LOGOUT);
                ActionList entryActionList = finishLogoutState.getExitActionList();
                entryActionList.add(new StoreServiceParamAction(casProperties));
                entryActionList.add(new ServiceRedirectAction(clientProvider()));
                LOGGER.debug("default web flow customization for delegateAuthentication 1st phase completed");
            }
        });
    }
    @Override
    public int getOrder() {
        // This CasWebflowExecutionPlanConfigurer must be run before SurrogateConfiguration to able to cancel auth
        // but after InterruptConfiguration to enable surrogate authentication after delegated authentication
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    private static <E, T extends Iterable<E>> void clear(T iterable, Consumer<E> remover) {
        StreamSupport.stream(iterable.spliterator(), false).collect(toList()).forEach(remover);
    }

}

