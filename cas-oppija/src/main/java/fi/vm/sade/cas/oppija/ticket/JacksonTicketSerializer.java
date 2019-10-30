package fi.vm.sade.cas.oppija.ticket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.apereo.cas.ticket.Ticket;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JacksonTicketSerializer implements TicketSerializer {

    private final ObjectMapper objectMapper;

    public JacksonTicketSerializer() {
        this(new ObjectMapper()
                .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL)
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()));
    }

    protected JacksonTicketSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String toJson(Ticket ticket) {
        try {
            return objectMapper.writeValueAsString(ticket);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Ticket fromJson(String ticketJson, String ticketGrantingTicketJson) {
        try {
            if (ticketGrantingTicketJson != null) {
                Ticket ticket = objectMapper.readValue(ticketJson, Ticket.class);
                ObjectReader objectReader = objectMapper.readerForUpdating(ticket);
                JsonNode ticketGrantingTicketNode = objectMapper.createObjectNode()
                        .set("ticketGrantingTicket", objectMapper.readTree(ticketGrantingTicketJson));
                return objectReader.readValue(ticketGrantingTicketNode);
            }
            return objectMapper.readValue(ticketJson, Ticket.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
