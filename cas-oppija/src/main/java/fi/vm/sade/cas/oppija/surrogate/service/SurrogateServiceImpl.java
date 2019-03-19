package fi.vm.sade.cas.oppija.surrogate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.cas.oppija.surrogate.*;
import fi.vm.sade.cas.oppija.surrogate.exception.SurrogateNotAllowedException;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.security.auth.login.LoginException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

import static fi.vm.sade.cas.oppija.surrogate.SurrogateConstants.AUTHORIZATION_HEADER;

public class SurrogateServiceImpl implements SurrogateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateServiceImpl.class);

    private final OphHttpClient httpClient;
    private final SurrogateProperties properties;
    private final ObjectMapper objectMapper;
    private final SurrogateSessionStorage surrogateSessionStorage;
    private final SurrogateTokenProvider surrogateTokenProvider;

    public SurrogateServiceImpl(OphHttpClient httpClient,
                                SurrogateProperties properties,
                                ObjectMapper objectMapper,
                                SurrogateSessionStorage surrogateSessionStorage,
                                SurrogateTokenProvider surrogateTokenProvider) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.surrogateSessionStorage = surrogateSessionStorage;
        this.surrogateTokenProvider = surrogateTokenProvider;
    }

    public String getAuthorizeUrl(SurrogateSession session, Function<String, String> tokenToRedirectUrl) {
        UriComponents host = UriComponentsBuilder.fromHttpUrl(properties.getHost()).build();
        String requestId = UUID.randomUUID().toString();
        String token = surrogateTokenProvider.createToken();
        String redirectUrl = tokenToRedirectUrl.apply(token);

        RegistrationDto registrationDto = getRegistration(host, session.nationalIdentificationNumber, requestId);
        session.update(redirectUrl, requestId, registrationDto.sessionId, registrationDto.userId);
        surrogateSessionStorage.add(token, session);

        return UriComponentsBuilder.fromPath("/oauth/authorize")
                .queryParam("client_id", properties.getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUrl)
                .queryParam("user", registrationDto.userId)
                .queryParam("lang", session.language)
                .uriComponents(host)
                .toUriString();
    }

    private RegistrationDto getRegistration(UriComponents host, String nationalIdentificationNumber, String requestId) {
        UriComponents path = UriComponentsBuilder.fromPath("/service/hpa/user/register/{client_id}/{hetu}")
                .queryParam("requestId", requestId)
                .buildAndExpand(properties.getClientId(), nationalIdentificationNumber);
        String url = UriComponentsBuilder.newInstance().uriComponents(host).uriComponents(path).toUriString();
        return httpClient.get(url)
                .header(AUTHORIZATION_HEADER, properties.getChecksum(path.toUriString(), Instant.now()))
                .doNotSendOphHeaders()
                .expectStatus(200)
                .execute(response -> objectMapper.readValue(response.asInputStream(), RegistrationDto.class));
    }

    public SurrogateAuthenticationDto getAuthentication(String token, String code) throws GeneralSecurityException {
        SurrogateSession session = surrogateSessionStorage.remove(token);
        if (session == null) {
            String message = String.format("Session '%s' does not exist", token);
            LOGGER.warn(message);
            throw new LoginException(message);
        }
        if (session.created.plus(properties.getSessionTimeout()).isBefore(Instant.now())) {
            String message = String.format("Session '%s' is expired", token);
            LOGGER.warn(message);
            throw new LoginException(message);
        }
        return getAuthentication(session, code);
    }

    private SurrogateAuthenticationDto getAuthentication(SurrogateSession session, String code) throws GeneralSecurityException {
        UriComponents host = UriComponentsBuilder.fromHttpUrl(properties.getHost()).build();

        AccessTokenDto accessToken = getAccessToken(host, session, code);
        PersonDto person = getSelectedPerson(host, session, accessToken);
        AuthorizationDto authorization = getAuthorization(host, session, accessToken, person);

        if (!"ALLOWED".equals(authorization.result)) {
            throw new SurrogateNotAllowedException(String.format("User is not allowed to authenticate as %s (result=%s)",
                    person.personId, authorization.result));
        }
        return new SurrogateAuthenticationDto(session.principalId, session.nationalIdentificationNumber,
                session.personOid, session.personName, person.personId, person.name);
    }

    private AccessTokenDto getAccessToken(UriComponents host, SurrogateSession session, String code) {
        String url = UriComponentsBuilder.fromPath("/oauth/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", session.redirectUrl)
                .queryParam("code", code)
                .uriComponents(host)
                .toUriString();
        return httpClient.post(url)
                .header("Authorization", "Basic " + properties.getCredentials())
                .doNotSendOphHeaders()
                .expectStatus(200)
                .execute(response -> objectMapper.readValue(response.asInputStream(), AccessTokenDto.class));
    }

    private PersonDto getSelectedPerson(UriComponents host, SurrogateSession session, AccessTokenDto accessTokenDto) {
        UriComponents path = UriComponentsBuilder.fromPath("/service/hpa/api/delegate/{sessionId}")
                .queryParam("requestId", session.requestId)
                .buildAndExpand(session.sessionId);
        String url = UriComponentsBuilder.newInstance().uriComponents(host).uriComponents(path).toUriString();
        return httpClient.get(url)
                .header("Authorization", "Bearer " + accessTokenDto.accessToken)
                .header(AUTHORIZATION_HEADER, properties.getChecksum(path.toUriString(), Instant.now()))
                .doNotSendOphHeaders()
                .expectStatus(200)
                .execute(response -> objectMapper.readValue(response.asInputStream(), PersonDto[].class))[0];
    }

    private AuthorizationDto getAuthorization(UriComponents host, SurrogateSession session,
                                              AccessTokenDto accessTokenDto, PersonDto personDto) {
        UriComponents path = UriComponentsBuilder.fromPath("/service/hpa/api/authorization/{sessionId}/{personId}")
                .queryParam("requestId", session.requestId)
                .buildAndExpand(session.sessionId, personDto.personId);
        String url = UriComponentsBuilder.newInstance().uriComponents(host).uriComponents(path).toUriString();
        return httpClient.get(url)
                .header("Authorization", "Bearer " + accessTokenDto.accessToken)
                .header(AUTHORIZATION_HEADER, properties.getChecksum(path.toUriString(), Instant.now()))
                .doNotSendOphHeaders()
                .expectStatus(200)
                .execute(response -> objectMapper.readValue(response.asInputStream(), AuthorizationDto.class));
    }

}
