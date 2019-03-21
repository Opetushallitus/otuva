package fi.vm.sade.cas.oppija.controller;

import fi.vm.sade.cas.oppija.exception.BadRequestException;
import fi.vm.sade.cas.oppija.exception.UnauthorizedException;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static fi.vm.sade.cas.oppija.controller.ControllerUtils.wrapExceptionToApplicationException;

@RestController
@RequestMapping("/user")
public class UserController {

    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private final TicketRegistry ticketRegistry;
    private final ArgumentExtractor argumentExtractor;
    private final ServicesManager servicesManager;

    public UserController(@Qualifier("ticketGrantingTicketCookieGenerator") CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                          TicketRegistry ticketRegistry,
                          ArgumentExtractor argumentExtractor,
                          ServicesManager servicesManager) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.ticketRegistry = ticketRegistry;
        this.argumentExtractor = argumentExtractor;
        this.servicesManager = servicesManager;
    }

    @GetMapping(value = "/current/attributes", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getAttributes(HttpServletRequest request) {
        return wrapExceptionToApplicationException(() -> getAttributesInternal(request));
    }

    private Map<String, Object> getAttributesInternal(HttpServletRequest request) {
        Principal principal = getPrincipal(request);
        Service service = getService(request);
        RegisteredService registeredService = getRegisteredService(service);
        RegisteredServiceAttributeReleasePolicy attributeReleasePolicy = registeredService.getAttributeReleasePolicy();
        return attributeReleasePolicy.getAttributes(principal, service, registeredService);
    }

    private Principal getPrincipal(HttpServletRequest request) {
        TicketGrantingTicket ticketGrantingTicket = getTicketGrantingTicket(request);
        Authentication authentication = ticketGrantingTicket.getAuthentication();
        return authentication.getPrincipal();
    }

    private TicketGrantingTicket getTicketGrantingTicket(HttpServletRequest request) {
        String ticket = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        if (ticket == null) {
            throw new UnauthorizedException("Ticket granting ticket was not provided");
        }
        TicketGrantingTicket ticketGrantingTicket = ticketRegistry.getTicket(ticket, TicketGrantingTicket.class);
        if (ticketGrantingTicket == null) {
            throw new UnauthorizedException("Ticket granting ticket doesn't exist");
        }
        return ticketGrantingTicket;
    }

    private Service getService(HttpServletRequest request) {
        WebApplicationService service = argumentExtractor.extractService(request);
        if (service == null) {
            throw new BadRequestException("Required parameter 'service' is missing");
        }
        return service;
    }

    private RegisteredService getRegisteredService(Service service) {
        RegisteredService registeredService = servicesManager.findServiceBy(service);
        if (registeredService == null) {
            throw new BadRequestException(String.format("Service '%s' is unauthorized", service));
        }
        return registeredService;
    }

}
