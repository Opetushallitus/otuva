package fi.vm.sade.cas.oppija.surrogate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.cas.oppija.surrogate.*;
import fi.vm.sade.cas.oppija.surrogate.exception.SurrogateNotAllowedException;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.LoginException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.function.Function;

@Service
@Transactional
public class SurrogateServiceImpl implements SurrogateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateServiceImpl.class);
    private final static String ATTRIBUTE_NAME_SURROGATE = "surrogate";

    private final OphHttpClient httpClient;
    private final SurrogateProperties properties;
    private final ObjectMapper objectMapper;
    private final TicketRegistry ticketRegistry;
    private final TransientSessionTicketFactory transientSessionTicketFactory;

    public SurrogateServiceImpl(OphHttpClient httpClient,
                                SurrogateProperties properties,
                                ObjectMapper objectMapper,
                                TicketRegistry ticketRegistry,
                                TransientSessionTicketFactory transientSessionTicketFactory) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.ticketRegistry = ticketRegistry;
        this.transientSessionTicketFactory = transientSessionTicketFactory;
    }

    public String getAuthorizeUrl(String nationalIdentificationNumber, String language,
                                  SurrogateImpersonatorData impersonatorData, Function<String, String> tokenToRedirectUrl) {
        TransientSessionTicket ticket = transientSessionTicketFactory.create(null);
        String token = ticket.getId();

        String requestId = UUID.randomUUID().toString();
        String redirectUrl = tokenToRedirectUrl.apply(token);
        SurrogateRequestHelper requestHelper = new SurrogateRequestHelper(httpClient, objectMapper, properties, requestId);

        RegistrationDto registrationDto = requestHelper.getRegistration(nationalIdentificationNumber);

        ticket.put(ATTRIBUTE_NAME_SURROGATE, new SurrogateData(impersonatorData,
                new SurrogateRequestData(redirectUrl, requestId, registrationDto.sessionId)));
        ticketRegistry.addTicket(ticket);

        return requestHelper.getAuthorizeUrl(redirectUrl, registrationDto.userId, language);
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
        SurrogateRequestHelper requestHelper = new SurrogateRequestHelper(httpClient, objectMapper, properties, data.requestData);

        AccessTokenDto accessToken = requestHelper.getAccessToken(code);
        PersonDto person = requestHelper.getSelectedPerson(accessToken.accessToken);
        AuthorizationDto authorization = requestHelper.getAuthorization(accessToken.accessToken, person.nationalIdentificationNumber);

        if (!"ALLOWED".equals(authorization.result)) {
            throw new SurrogateNotAllowedException(String.format("User is not allowed to authenticate as %s (result=%s)",
                    person.nationalIdentificationNumber, authorization.result));
        }
        return new SurrogateAuthenticationDto(data.impersonatorData, person.nationalIdentificationNumber, person.name);
    }

}
