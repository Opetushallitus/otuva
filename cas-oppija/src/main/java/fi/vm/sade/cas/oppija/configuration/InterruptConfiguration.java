package fi.vm.sade.cas.oppija.configuration;


import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.interrupt.webflow.InterruptWebflowConfigurer;
import org.apereo.cas.interrupt.webflow.actions.InquireInterruptAction;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionList;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.TransitionSet;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;


/**
 * This class should include only fixes to default cas interrupt configuration.
 *
 * @see InterruptInquiryExecutionPlanConfiguration actual interrupt configuration
 */

@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class InterruptConfiguration implements CasWebflowExecutionPlanConfigurer {

    private final FlowBuilderServices flowBuilderServices;
    private final FlowDefinitionRegistry loginFlowDefinitionRegistry;
    private final ConfigurableApplicationContext applicationContext;
    private final CasConfigurationProperties casProperties;
    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    public InterruptConfiguration(FlowBuilderServices flowBuilderServices,
                                  @Qualifier("loginFlowRegistry") FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                  ConfigurableApplicationContext applicationContext,
                                  CasConfigurationProperties casProperties,
                                  @Qualifier("ticketGrantingTicketCookieGenerator")
                                  CasCookieBuilder ticketGrantingTicketCookieGenerator
    ) {
        this.flowBuilderServices = flowBuilderServices;
        this.loginFlowDefinitionRegistry = loginFlowDefinitionRegistry;
        this.applicationContext = applicationContext;
        this.casProperties = casProperties;
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    @Override
    public void configureWebflowExecutionPlan(CasWebflowExecutionPlan plan) {
        // this is from default interruptWebflowConfigurer bean:
        //plan.registerWebflowConfigurer(new InterruptWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties));

        plan.registerWebflowConfigurer(new AbstractCasWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            protected void doInitialize() {
                // add redirect transition
                ActionState inquireInterruptAction = getState(getLoginFlow(), "inquireInterruptAction", ActionState.class);
                TransitionSet transitions = inquireInterruptAction.getTransitionSet();
                transitions.add(createTransition("interruptRedirect", "redirectInterrupt"));
                createEndState(getLoginFlow(), "redirectInterrupt", "flowScope.interruptRedirectUrl", true);
            }
        });
        plan.registerWebflowConfigurer(new AbstractCasWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            protected void doInitialize() {
                // fix interrupt inquirers called twice after successful login
                ActionState state = getState(getLoginFlow(), CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, ActionState.class);
                ActionList actions = state.getActionList();
                clear(actions, actions::remove);
                actions.add(super.createEvaluateAction(CasWebflowConstants.ACTION_ID_CREATE_TICKET_GRANTING_TICKET));
            }
        });

    }

    private static <E, T extends Iterable<E>> void clear(T iterable, Consumer<E> remover) {
        StreamSupport.stream(iterable.spliterator(), false).collect(toList()).forEach(remover::accept);
    }

    // override default interruptWebflowConfigurer to be able to override its flow definitions (see above)
    @Bean
    public CasWebflowConfigurer interruptWebflowConfigurer() {
        return new InterruptWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties) {
            @Override
            public int getOrder() {
                // This CasWebflowExecutionPlanConfigurer must be run before DelegatedAuthenticationConfiguration to enable
                // surrogate authentication after delegated authentication
                return Ordered.HIGHEST_PRECEDENCE;
            }
        };

    }

    // override default inquireInterruptAction to add new interruptRedirect transition
    @Bean
    public InquireInterruptAction inquireInterruptAction(List<InterruptInquirer> interruptInquirers) {
        return new InquireInterruptAction(interruptInquirers, casProperties, ticketGrantingTicketCookieGenerator) {
            @Override
            protected Event doExecute(RequestContext requestContext) {
                Event event = super.doExecute(requestContext);
                if (CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED.equals(event.getId())) {
                    InterruptResponse interruptResponse = InterruptUtils.getInterruptFrom(requestContext);
                    if (interruptResponse.isAutoRedirect() && interruptResponse.getAutoRedirectAfterSeconds() < 0
                            && interruptResponse.getLinks().size() > 0) {
                        requestContext.getFlowScope().put("interruptRedirectUrl",
                                interruptResponse.getLinks().values().iterator().next());
                        return result("interruptRedirect");
                    }
                }
                return event;
            }
        };
    }

}

