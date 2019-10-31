package fi.vm.sade.cas.oppija.configuration;

import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class TaskConfiguration {

    @Bean
    public Task<Void> ticketRegistryCleanerTask(CasConfigurationProperties casProperties, TicketRegistryCleaner ticketRegistryCleaner) {
        Duration duration = Duration.parse(casProperties.getTicket().getRegistry().getCleaner().getSchedule().getRepeatInterval());
        return Tasks.recurring("cas-ticket-registry-cleaner", FixedDelay.of(duration))
                .execute((taskInstance, executionContext) -> ticketRegistryCleaner.clean());
    }

}
