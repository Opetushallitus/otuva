package fi.vm.sade.cas.oppija;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@ComponentScan("fi.vm.sade.cas.oppija")
public class CasOppijaConfiguration {
    @Bean
    public TicketSerializationExecutionPlanConfigurer ticketSerializationExecutionPlanConfigurer() {
        LOGGER.info("Initializing ticketSerializationExecutionPlanConfigurer");
        return plan -> {
            plan.registerTicketSerializer(new CasOppijaTransientSessionTicketSerializer());
        };
    }
}
