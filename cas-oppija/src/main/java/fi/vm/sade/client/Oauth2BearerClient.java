package fi.vm.sade.client;

import static fi.vm.sade.CacheConfiguration.CACHE_NAME_OAUTH2_BEARER;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@CacheConfig(cacheNames = CACHE_NAME_OAUTH2_BEARER)
@RequiredArgsConstructor
public class Oauth2BearerClient {
    private final Gson gson = new Gson();

    @Value("${kayttooikeus.baseurl}")
    private String kayttooikeusBaseurl;
    @Value("${oauth2.client-id}")
    private String clientId;
    @Value("${oauth2.client-secret}")
    private String clientSecret;

    @Cacheable(value = CACHE_NAME_OAUTH2_BEARER, sync = true)
    public String getOauth2Bearer() throws IOException, InterruptedException {
        String tokenUrl = kayttooikeusBaseurl + "/oauth2/token";
        LOGGER.info("refetching oauth2 bearer from " + tokenUrl);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(encodeFormBody(Map.of(
                        "grant_type", "client_credentials",
                        "client_id", clientId,
                        "client_secret", clientSecret
                )))
                .build();
        var client = HttpClient.newHttpClient();
        HttpResponse<String> res = client.send(request, BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new RuntimeException("Oauth2 bearer returned status code " + res.statusCode() + ": " + res.body());
        }
        LOGGER.info("oauth2 bearer body: " + res.body());
        return gson.fromJson(res.body(), Oauth2Token.class).access_token();
    }

    @CacheEvict(value = CACHE_NAME_OAUTH2_BEARER, allEntries = true)
    public void evictOauth2Bearer() {
        LOGGER.info("evicting oauth2 bearer cache");
    }

    HttpRequest.BodyPublisher encodeFormBody(Map<String, String> params) {
        var body = params.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
        return HttpRequest.BodyPublishers.ofString(body);
    }

    String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    record Oauth2Token(String access_token, String token_type, Integer expires_in) {}
}
