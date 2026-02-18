package fi.vm.sade.cas.oppija.controller;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;
    private final TicketRegistry ticketRegistry;
    private final ArgumentExtractor argumentExtractor;
    private final ServicesManager servicesManager;

    public UserController(@Qualifier("ticketGrantingTicketCookieGenerator") CasCookieBuilder ticketGrantingTicketCookieGenerator,
                          TicketRegistry ticketRegistry,
                          ArgumentExtractor argumentExtractor,
                          ServicesManager servicesManager) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.ticketRegistry = ticketRegistry;
        this.argumentExtractor = argumentExtractor;
        this.servicesManager = servicesManager;
    }

    @GetMapping(value = "/current/attributes", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Object>> getAttributes(HttpServletRequest request) {
        return getAttributesInternal(request);
    }

    private Map<String, List<Object>> getAttributesInternal(HttpServletRequest request) {
        Principal principal = getPrincipal(request);
        Service service = getService(request);
        RegisteredService registeredService = getRegisteredService(service);
        RegisteredServiceAttributeReleasePolicy attributeReleasePolicy = registeredService.getAttributeReleasePolicy();
        RegisteredServiceAttributeReleasePolicyContext context = RegisteredServiceAttributeReleasePolicyContext.builder().principal(principal).service(service).registeredService(registeredService).build();

        try {
            return attributeReleasePolicy.getAttributes(context);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private Principal getPrincipal(HttpServletRequest request) {
        TicketGrantingTicket ticketGrantingTicket = getTicketGrantingTicket(request);
        Authentication authentication = ticketGrantingTicket.getAuthentication();
        return authentication.getPrincipal();
    }

    private TicketGrantingTicket getTicketGrantingTicket(HttpServletRequest request) {
        String ticket = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        if (ticket == null) {
            throw new RuntimeException("Ticket granting ticket was not provided");
        }
        TicketGrantingTicket ticketGrantingTicket = ticketRegistry.getTicket(ticket, TicketGrantingTicket.class);
        if (ticketGrantingTicket == null) {
            throw new RuntimeException("Ticket granting ticket doesn't exist");
        }
        return ticketGrantingTicket;
    }

    private Service getService(HttpServletRequest request) {
        WebApplicationService service = argumentExtractor.extractService(request);
        if (service == null) {
            throw new RuntimeException("Required parameter 'service' is missing");
        }
        return service;
    }

    private RegisteredService getRegisteredService(Service service) {
        RegisteredService registeredService = servicesManager.findServiceBy(service);
        if (registeredService == null) {
            throw new RuntimeException(String.format("Service '%s' is unauthorized", service));
        }
        return registeredService;
    }

}
