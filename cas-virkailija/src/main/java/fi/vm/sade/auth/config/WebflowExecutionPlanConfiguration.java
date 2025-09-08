package fi.vm.sade.auth.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WebflowExecutionPlanConfiguration implements CasWebflowExecutionPlanConfigurer {
    private final FlowBuilderServices flowBuilderServices;
    private final FlowDefinitionRegistry flowDefinitionRegistry;
    private final ConfigurableApplicationContext applicationContext;
    private final CasConfigurationProperties casProperties;

    public WebflowExecutionPlanConfiguration(FlowBuilderServices flowBuilderServices,
                                             @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY) FlowDefinitionRegistry flowDefinitionRegistry,
                                             ConfigurableApplicationContext applicationContext,
                                             CasConfigurationProperties casProperties) {
        this.flowBuilderServices = flowBuilderServices;
        this.flowDefinitionRegistry = flowDefinitionRegistry;
        this.applicationContext = applicationContext;
        this.casProperties = casProperties;
    }

    @Override
    public void configureWebflowExecutionPlan(CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(new WebflowConfiguration(flowBuilderServices, flowDefinitionRegistry,
                applicationContext, casProperties));
    }

}
