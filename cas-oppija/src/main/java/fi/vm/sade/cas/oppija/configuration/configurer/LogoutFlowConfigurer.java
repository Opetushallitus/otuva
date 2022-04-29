package fi.vm.sade.cas.oppija.configuration.configurer;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.DefaultLogoutWebflowConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

public class LogoutFlowConfigurer extends DefaultLogoutWebflowConfigurer {

    public LogoutFlowConfigurer(final FlowBuilderServices flowBuilderServices,
                                final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                final ConfigurableApplicationContext applicationContext,
                                final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        // add logout flow to login flow to be able logout from delegatedAuthenticationAction
        Flow flow = getLogoutFlow();
        StateDefinition startState = flow.getStartState();
        flow.setStartState(startState.getId());
    }

    @Override
    public Flow getLogoutFlow() {
        return getLoginFlow();
    }
}