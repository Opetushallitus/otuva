package fi.vm.sade.cas.oppija.configuration;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.NoOpLockingStrategy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TicketConfiguration {

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner(LogoutManager logoutManager, TicketRegistry ticketRegistry) {
        return new DefaultTicketRegistryCleaner(new NoOpLockingStrategy(), logoutManager, ticketRegistry);
    }

}
