package fi.vm.sade.cas.oppija.ticket;

import org.apereo.cas.ticket.Ticket;

public interface TicketSerializer {

    String toJson(Ticket ticket);

    Ticket fromJson(String ticketJson, String ticketGrantingTicketJson);

}
