package fi.vm.sade.auth.discovery;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.support.ActionExecutingViewFactory;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SamlDiscoveryWebflowConfigurer extends AbstractCasWebflowConfigurer {
    private final FlowDefinitionRegistry redirectFlowRegistry;
    private final DelegatedClientAuthenticationConfigurationContext configContext;

    public SamlDiscoveryWebflowConfigurer(FlowBuilderServices flowBuilderServices,
                                          FlowDefinitionRegistry mainFlowDefinitionRegistry,
                                          FlowDefinitionRegistry redirectFlowRegistry,
                                          DelegatedClientAuthenticationConfigurationContext configContext,
                                          ConfigurableApplicationContext applicationContext,
                                          CasConfigurationProperties casProperties) {
        super(flowBuilderServices, mainFlowDefinitionRegistry, applicationContext, casProperties);
        this.redirectFlowRegistry = redirectFlowRegistry;
        this.configContext = configContext;
    }

    /**
     * Replace the current start state with newly created discoveryState and corresponding action.
     */
    @Override
    protected void doInitialize() {
        val redirectFlow = redirectFlowRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_DELEGATION_REDIRECT);
        val storeWebflowAction = redirectFlow.getState(CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_STORE);

        LOGGER.info("Adding discovery state to the [{}] delegation authentication redirect flow.", redirectFlow.getId());
        val discoveryState = createActionState((Flow)redirectFlow,
                SamlDiscoveryWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_DISCOVERY,
                SamlDiscoveryWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_DISCOVERY);
        createTransitionForState(discoveryState,
                SamlDiscoveryWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_SUCCESS,
                CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_STORE);
        createTransitionForState(discoveryState,
                SamlDiscoveryWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_REDIRECT,
                SamlDiscoveryWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_REDIRECT_TO_DISCOVERY);

        val factory = new ActionExecutingViewFactory(
                new SamlDiscoveryRedirectAction(getCasProperties(), configContext));
        val viewState = createViewState((Flow) redirectFlow,
                SamlDiscoveryWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_REDIRECT_TO_DISCOVERY, factory);

        val finalizeState = createActionState((Flow)redirectFlow,
                SamlDiscoveryWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_FINALIZE_DISCOVERY,
                SamlDiscoveryWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_FINALIZE_DISCOVERY);

        createStateDefaultTransition(viewState, finalizeState);
        createStateDefaultTransition(finalizeState, storeWebflowAction);

        createStateDefaultTransition(discoveryState, storeWebflowAction);
        setStartState((Flow) redirectFlow, discoveryState);
    }

}
