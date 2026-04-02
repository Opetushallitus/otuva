package fi.vm.sade.auth.clients;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.vm.sade.auth.cas.CasUserAttributes;
import fi.vm.sade.auth.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.function.Predicate.not;

@Slf4j
@Component
@RequiredArgsConstructor
public class KayttooikeusOauth2Client implements KayttooikeusClient {
    private final Oauth2Client httpClient;

    @Value("${kayttooikeus-service.baseurl}")
    private String baseurl;

    private record HenkiloDto(String oid) {};

    @Override
    public String getHenkiloOid(String username) {
        var path = "henkilo/kayttajatunnus=" + username;
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .GET();
        var response = Json.parse(httpClient.executeRequest(request).body(), HenkiloDto.class);
        return response.oid();
    }

    @Override
    public String createLoginToken(String henkiloOid) {
        var path = "cas/auth/henkilo/" + henkiloOid + "/loginToken?salasananVaihto=true";
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .GET();
        return Json.parse(httpClient.executeRequest(request).body(), String.class);
    }

    @Override
    public Optional<String> getRedirectCodeByUsername(String username) {
        var path = "cas/auth/henkilo/username/" + username + "/logInRedirect";
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .GET();
        var response = httpClient.executeRequest(request);
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get redirect code for username " + username);
        } else if (response.body() != null && response.body().length() > 0) {
            var redirectCode = Json.parse(response.body(), String.class);
            return Optional.ofNullable(redirectCode).map(String::trim).filter(not(String::isEmpty));
        } else {
            return Optional.empty();
        }
    }

    record Login(String username, String password) {}

    @Override
    public Optional<CasUserAttributes> getUserAttributesByUsernamePassword(String username, String password) {
        var path = "cas/auth";
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(Json.write(new Login(username, password))));
        var response = httpClient.executeRequest(request);
        if (response.statusCode() == 401) {
            return Optional.empty();
        }
        return Optional.of(Json.parse(response.body(), CasUserAttributes.class));
    }

    @Override
    public CasUserAttributes getHenkiloByAuthToken(String authToken) {
        var path = "cas/auth/token/" + authToken;
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .GET();
        return Json.parse(httpClient.executeRequest(request).body(), CasUserAttributes.class);
    }

    @Override
    public CasUserAttributes getUserAttributesByOid(String oid) {
        var path = "cas/auth/henkilo/" + oid;
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .GET();
        return Json.parse(httpClient.executeRequest(request).body(), CasUserAttributes.class);
    }

    @Override
    public CasUserAttributes getUserAttributesByIdpIdentifier(String idp, String identifier) {
        var path = "cas/auth/identification/" + idp + "/" + identifier;
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .GET();
        return Json.parse(httpClient.executeRequest(request).body(), CasUserAttributes.class);
    }

    record UserAttributesByHetu(String hetu) {};

    @Override
    public CasUserAttributes getUserAttributesByHetu(String hetu) {
        var path = "cas/auth/hetu";
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(Json.write(new UserAttributesByHetu(hetu))));
        return Json.parse(httpClient.executeRequest(request).body(), CasUserAttributes.class);
    }

    @Override
    public CasUserAttributes registerVirkailija(VirkailijaRegistration dto) {
        var path = "cas/register";
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(Json.write(dto)));
        return Json.parse(httpClient.executeRequest(request).body(), CasUserAttributes.class);
    }
}
