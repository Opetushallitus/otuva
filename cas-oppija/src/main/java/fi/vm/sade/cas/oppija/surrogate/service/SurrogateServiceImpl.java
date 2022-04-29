package fi.vm.sade.cas.oppija.surrogate.service;
/*

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.cas.oppija.surrogate.*;
import fi.vm.sade.cas.oppija.surrogate.exception.SurrogateNotAllowedException;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.suomifi.valtuudet.*;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import javax.security.auth.login.LoginException;
import java.security.GeneralSecurityException;

import static fi.vm.sade.cas.oppija.surrogate.SurrogateConstants.TOKEN_PARAMETER_NAME;

@Service
@Transactional
public class SurrogateServiceImpl implements SurrogateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateServiceImpl.class);
    private final static String ATTRIBUTE_NAME_SURROGATE = "surrogate";

    private final Environment environment;
    private final TicketRegistry ticketRegistry;
    private final TransientSessionTicketFactory transientSessionTicketFactory;
    private final ValtuudetClient valtuudetClient;

    public SurrogateServiceImpl(OphHttpClient httpClient,
                                ObjectMapper objectMapper,
                                SurrogateProperties properties,
                                Environment environment,
                                TicketRegistry ticketRegistry,
                                TransientSessionTicketFactory transientSessionTicketFactory) {
        this.environment = environment;
        this.ticketRegistry = ticketRegistry;
        this.transientSessionTicketFactory = transientSessionTicketFactory;
        this.valtuudetClient = new ValtuudetClientImpl(httpClient, objectMapper::readValue, properties);
    }

    public String getRedirectUrl(org.apereo.cas.authentication.principal.Service service, String nationalIdentificationNumber, String language,
                                 SurrogateImpersonatorData impersonatorData) {
        TransientSessionTicket ticket = transientSessionTicketFactory.create(service);

        String callbackUrl = createCallbackUrl(service, ticket.getId());
        SessionDto session = valtuudetClient.createSession(ValtuudetType.PERSON, nationalIdentificationNumber);
        String redirectUrl = valtuudetClient.getRedirectUrl(session.userId, callbackUrl, language);

        ticket.put(ATTRIBUTE_NAME_SURROGATE, new SurrogateData(impersonatorData,
                new SurrogateRequestData(callbackUrl, session.sessionId)));
        ticketRegistry.addTicket(ticket);

        return redirectUrl;
    }

    private String createCallbackUrl(org.apereo.cas.authentication.principal.Service service, String token) {
        String callbackUrl = environment.getRequiredProperty("cas.server.prefix") + "/login?" + TOKEN_PARAMETER_NAME + "=" + token;
        String serviceUrl = service != null ? service.getId() : null;
        if (serviceUrl != null) {
            callbackUrl = callbackUrl + "&service=" + serviceUrl;
        }
        LOGGER.info("CallbackUrl: " + callbackUrl);
        return callbackUrl;
    }

    public SurrogateAuthenticationDto getAuthentication(String token, String code) throws GeneralSecurityException {
        TransientSessionTicket ticket = ticketRegistry.getTicket(token, TransientSessionTicket.class);
        if (ticket == null) {
            String message = String.format("Session '%s' does not exist", token);
            LOGGER.warn(message);
            throw new LoginException(message);
        }
        if (ticket.isExpired()) {
            String message = String.format("Session '%s' is expired", token);
            LOGGER.warn(message);
            throw new LoginException(message);
        }
        SurrogateData data = ticket.get(ATTRIBUTE_NAME_SURROGATE, SurrogateData.class);
        if (data == null) {
            String message = String.format("Session '%s' doesn't contain surrogate data", token);
            LOGGER.warn(message);
            throw new LoginException(message);
        }
        ticketRegistry.deleteTicket(ticket);
        return getAuthentication(data, code);
    }

    private SurrogateAuthenticationDto getAuthentication(SurrogateData data, String code) throws GeneralSecurityException {
        try {
            return handleAuthentication(data, code);
        } finally {
            try {
                valtuudetClient.destroySession(ValtuudetType.PERSON, data.requestData.sessionId);
            } catch (Exception e) {
                LOGGER.warn("Unable to destroy valtuudet session", e);
            }
        }
    }

    private SurrogateAuthenticationDto handleAuthentication(SurrogateData data, String code) throws GeneralSecurityException {
        String accessToken = valtuudetClient.getAccessToken(code, data.requestData.redirectUrl);
        PersonDto person = valtuudetClient.getSelectedPerson(data.requestData.sessionId, accessToken);
        boolean authorized = valtuudetClient.isAuthorizedToPerson(data.requestData.sessionId, accessToken, person.personId);

        if (!authorized) {
            throw new SurrogateNotAllowedException(String.format("User is not allowed to authenticate as %s", person.personId));
        }
        return new SurrogateAuthenticationDto(data.impersonatorData, person.personId, person.name);
    }

}
*/
