package fi.vm.sade.auth.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
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
    private final FlowDefinitionRegistry loginFlowDefinitionRegistry;
    private final ConfigurableApplicationContext applicationContext;
    private final CasConfigurationProperties casProperties;
    private final CasWebflowConfigurer samlDiscoveryWebflowConfigurer;

    public WebflowExecutionPlanConfiguration(FlowBuilderServices flowBuilderServices,
                                             @Qualifier("loginFlowRegistry") FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                             ConfigurableApplicationContext applicationContext,
                                             CasConfigurationProperties casProperties,
                                             @Qualifier("samlDiscoveryWebflowConfigurer") final CasWebflowConfigurer samlDiscoveryWebflowConfigurer) {
        this.flowBuilderServices = flowBuilderServices;
        this.loginFlowDefinitionRegistry = loginFlowDefinitionRegistry;
        this.applicationContext = applicationContext;
        this.casProperties = casProperties;
        this.samlDiscoveryWebflowConfigurer = samlDiscoveryWebflowConfigurer;
    }

    @Override
    public void configureWebflowExecutionPlan(CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(new WebflowConfiguration(flowBuilderServices, loginFlowDefinitionRegistry,
                applicationContext, casProperties));
        plan.registerWebflowConfigurer(this.samlDiscoveryWebflowConfigurer);
    }

}
