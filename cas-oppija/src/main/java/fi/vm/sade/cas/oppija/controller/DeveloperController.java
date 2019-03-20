package fi.vm.sade.cas.oppija.controller;

import fi.vm.sade.cas.oppija.exception.ApplicationException;
import fi.vm.sade.cas.oppija.exception.BadRequestException;
import fi.vm.sade.cas.oppija.exception.SystemException;
import fi.vm.sade.cas.oppija.exception.UnauthorizedException;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.javautils.httpclient.OphHttpResponse;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

import static fi.vm.sade.javautils.httpclient.OphHttpClient.FORM_URLENCODED;
import static fi.vm.sade.javautils.httpclient.OphHttpClient.UTF8;

@RestController
@RequestMapping("/developer")
public class DeveloperController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeveloperController.class);

    private final CookieRetrievingCookieGenerator cookieRetrievingCookieGenerator;
    private final ArgumentExtractor argumentExtractor;
    private final ServicesManager servicesManager;
    private final OphHttpClient httpClient;
    private final Environment environment;

    public DeveloperController(@Qualifier("ticketGrantingTicketCookieGenerator") CookieRetrievingCookieGenerator cookieRetrievingCookieGenerator,
                               ArgumentExtractor argumentExtractor,
                               ServicesManager servicesManager,
                               OphHttpClient httpClient,
                               Environment environment) {
        this.cookieRetrievingCookieGenerator = cookieRetrievingCookieGenerator;
        this.argumentExtractor = argumentExtractor;
        this.servicesManager = servicesManager;
        this.httpClient = httpClient;
        this.environment = environment;
    }

    @GetMapping(value = "/serviceValidate", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getServiceValidate(HttpServletRequest request, @RequestParam(required = false, defaultValue = "XML") String format) {
        return getServiceValidate(request, Format.valueOf(format, Format.XML));
    }

    private ResponseEntity<String> getServiceValidate(HttpServletRequest request, @RequestParam(required = false, defaultValue = "XML") Format format) {
        String ticketGrantingTicket = cookieRetrievingCookieGenerator.retrieveCookieValue(request);
        if (ticketGrantingTicket == null) {
            throw new UnauthorizedException();
        }
        WebApplicationService service = argumentExtractor.extractService(request);
        String serviceUrl = service != null ? service.getId() : null;
        if (serviceUrl == null) {
            throw new BadRequestException("Required parameter 'service' is missing");
        }
        if (!servicesManager.matchesExistingService(serviceUrl)) {
            throw new BadRequestException("Service is unauthorized");
        }
        try {
            String serviceTicket = getServiceTicket(ticketGrantingTicket, serviceUrl);
            return getServiceValidate(serviceTicket, serviceUrl, format);
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    private String getServiceTicket(String ticketGrantingTicket, String serviceUrl) {
        String serviceTicketUrl = UriComponentsBuilder
                .fromHttpUrl(environment.getRequiredProperty("cas.server.prefix"))
                .path("/v1/tickets/" + ticketGrantingTicket)
                .build()
                .toUriString();
        return httpClient.post(serviceTicketUrl)
                .dataWriter(FORM_URLENCODED, UTF8, out -> OphHttpClient.formUrlEncodedWriter(out).param("service", serviceUrl))
                .accept(MediaType.TEXT_PLAIN_VALUE)
                .expectStatus(200)
                .execute(OphHttpResponse::asText);
    }

    private ResponseEntity<String> getServiceValidate(String serviceTicket, String serviceUrl, Format format) {
        String serviceValidateUrl = UriComponentsBuilder
                .fromHttpUrl(environment.getRequiredProperty("cas.server.prefix"))
                .path("/p3/serviceValidate")
                .queryParam("service", serviceUrl)
                .queryParam("ticket", serviceTicket)
                .queryParam("format", format.name())
                .build()
                .toUriString();
        return httpClient.get(serviceValidateUrl)
                .expectStatus(200)
                .execute(response -> ophResponseToSpringResponse(response, format));
    }

    private static ResponseEntity<String> ophResponseToSpringResponse(OphHttpResponse response, Format format) {
        return ResponseEntity.status(response.getStatusCode())
                .contentType(format.contentType)
                .body(response.asText());
    }

    private enum Format {

        XML(MediaType.APPLICATION_XML),
        JSON(MediaType.APPLICATION_JSON);

        private final MediaType contentType;

        Format(MediaType contentType) {
            this.contentType = contentType;
        }

        public static Format valueOf(String name, Format defaultValue) {
            try {
                return Format.valueOf(name);
            } catch (IllegalArgumentException e) {
                return defaultValue;
            }
        }

    }

}
