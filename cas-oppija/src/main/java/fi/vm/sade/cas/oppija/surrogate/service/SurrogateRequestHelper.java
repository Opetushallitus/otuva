package fi.vm.sade.cas.oppija.surrogate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.cas.oppija.surrogate.SurrogateProperties;
import fi.vm.sade.cas.oppija.surrogate.SurrogateRequestData;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;

import static fi.vm.sade.cas.oppija.surrogate.SurrogateConstants.AUTHORIZATION_HEADER;

public class SurrogateRequestHelper {

    private final OphHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SurrogateProperties properties;
    private final UriComponents host;
    private final String requestId;
    private String redirectUrl;
    private String sessionId;

    public SurrogateRequestHelper(OphHttpClient httpClient, ObjectMapper objectMapper, SurrogateProperties properties,
                                  String requestId) {
        this(httpClient, objectMapper, properties, requestId, null, null);
    }

    public SurrogateRequestHelper(OphHttpClient httpClient, ObjectMapper objectMapper, SurrogateProperties properties,
                                  SurrogateRequestData data) {
        this(httpClient, objectMapper, properties, data.requestId, data.redirectUrl, data.sessionId);
    }

    private SurrogateRequestHelper(OphHttpClient httpClient, ObjectMapper objectMapper, SurrogateProperties properties,
                                   String requestId, String redirectUrl, String sessionId) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.host = UriComponentsBuilder.fromHttpUrl(properties.getHost()).build();
        this.requestId = requestId;
        this.redirectUrl = redirectUrl;
        this.sessionId = sessionId;
    }

    public RegistrationDto getRegistration(String nationalIdentificationNumber) {
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

    public String getAuthorizeUrl(String redirectUrl, String userId, String language) {
        return UriComponentsBuilder.fromPath("/oauth/authorize")
                .queryParam("client_id", properties.getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUrl)
                .queryParam("user", userId)
                .queryParam("lang", language)
                .uriComponents(host)
                .toUriString();
    }

    public AccessTokenDto getAccessToken(String code) {
        String url = UriComponentsBuilder.fromPath("/oauth/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", redirectUrl)
                .queryParam("code", code)
                .uriComponents(host)
                .toUriString();

        return httpClient.post(url)
                .header("Authorization", "Basic " + properties.getCredentials())
                .doNotSendOphHeaders()
                .expectStatus(200)
                .execute(response -> objectMapper.readValue(response.asInputStream(), AccessTokenDto.class));
    }

    public PersonDto getSelectedPerson(String accessToken) {
        UriComponents path = UriComponentsBuilder.fromPath("/service/hpa/api/delegate/{sessionId}")
                .queryParam("requestId", requestId)
                .buildAndExpand(sessionId);
        String url = UriComponentsBuilder.newInstance().uriComponents(host).uriComponents(path).toUriString();

        return httpClient.get(url)
                .header("Authorization", "Bearer " + accessToken)
                .header(AUTHORIZATION_HEADER, properties.getChecksum(path.toUriString(), Instant.now()))
                .doNotSendOphHeaders()
                .expectStatus(200)
                .execute(response -> objectMapper.readValue(response.asInputStream(), PersonDto[].class))[0];
    }

    public AuthorizationDto getAuthorization(String accessToken, String nationalIdentificationNumber) {
        UriComponents path = UriComponentsBuilder.fromPath("/service/hpa/api/authorization/{sessionId}/{personId}")
                .queryParam("requestId", requestId)
                .buildAndExpand(sessionId, nationalIdentificationNumber);
        String url = UriComponentsBuilder.newInstance().uriComponents(host).uriComponents(path).toUriString();

        return httpClient.get(url)
                .header("Authorization", "Bearer " + accessToken)
                .header(AUTHORIZATION_HEADER, properties.getChecksum(path.toUriString(), Instant.now()))
                .doNotSendOphHeaders()
                .expectStatus(200)
                .execute(response -> objectMapper.readValue(response.asInputStream(), AuthorizationDto.class));
    }

}
