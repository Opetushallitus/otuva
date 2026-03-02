package fi.vm.sade.auth.clients;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import fi.vm.sade.auth.cas.CasUserAttributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.function.Predicate.not;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "oauth2.enabled", havingValue = "true")
public class KayttooikeusOauth2Client implements KayttooikeusClient {
    private final Oauth2Client httpClient;
    private final Gson gson = new Gson();

    @Value("${kayttooikeus-service.baseurl}")
    private String baseurl;

    private record HenkiloDto(String oid) {};

    @Override
    public String getHenkiloOid(String username) {
        var path = "henkilo/kayttajatunnus=" + username;
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .GET();
        var response = gson.fromJson(httpClient.executeRequest(request).body(), HenkiloDto.class);
        return response.oid();
    }

    @Override
    public String createLoginToken(String henkiloOid) {
        var path = "cas/auth/henkilo/" + henkiloOid + "/loginToken?salasananVaihto=true";
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .GET();
        return gson.fromJson(httpClient.executeRequest(request).body(), String.class);
    }

    @Override
    public Optional<String> getRedirectCodeByUsername(String username) {
        var path = "cas/auth/henkilo/username/" + username + "/logInRedirect";
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .GET();
        var response = gson.fromJson(httpClient.executeRequest(request).body(), String.class);
        return Optional.ofNullable(response).map(String::trim).filter(not(String::isEmpty));
    }

    record Login(String username, String password) {}

    @Override
    public Optional<CasUserAttributes> getUserAttributesByUsernamePassword(String username, String password) {
        var path = "cas/auth";
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(gson.toJson(new Login(username, password))));
        var response = httpClient.executeRequest(request);
        if (response.statusCode() == 401) {
            return Optional.empty();
        }
        return Optional.of(gson.fromJson(response.body(), CasUserAttributes.class));
    }

    @Override
    public CasUserAttributes getHenkiloByAuthToken(String authToken) {
        var path = "cas/auth/token/" + authToken;
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .GET();
        return gson.fromJson(httpClient.executeRequest(request).body(), CasUserAttributes.class);
    }

    @Override
    public CasUserAttributes getUserAttributesByOid(String oid) {
        var path = "cas/auth/henkilo/" + oid;
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .GET();
        return gson.fromJson(httpClient.executeRequest(request).body(), CasUserAttributes.class);
    }

    @Override
    public CasUserAttributes getUserAttributesByIdpIdentifier(String idp, String identifier) {
        var path = "cas/auth/identification/" + idp + "/" + identifier;
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .GET();
        return gson.fromJson(httpClient.executeRequest(request).body(), CasUserAttributes.class);
    }

    record UserAttributesByHetu(String hetu) {};

    @Override
    public CasUserAttributes getUserAttributesByHetu(String hetu) {
        var path = "cas/auth/hetu";
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(gson.toJson(new UserAttributesByHetu(hetu))));
        return gson.fromJson(httpClient.executeRequest(request).body(), CasUserAttributes.class);
    }

    record HakaRegistration(String hakaIdentifier) {};

    @Override
    public CasUserAttributes hakaRegistration(String temporaryToken, String identifier) {
        var path = "cas/hakaregistration/" + temporaryToken;
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(gson.toJson(new HakaRegistration(identifier))));
        return gson.fromJson(httpClient.executeRequest(request).body(), CasUserAttributes.class);
    }

}
