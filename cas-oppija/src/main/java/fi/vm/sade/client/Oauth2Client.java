package fi.vm.sade.client;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class Oauth2Client {
    private final Oauth2BearerClient oauth2BearerClient;

    private HttpResponse<String> execute(HttpRequest.Builder requestBuilder) {
        try {
            var request = requestBuilder
                    .timeout(Duration.ofSeconds(35))
                    .setHeader("Authorization", "Bearer " + oauth2BearerClient.getOauth2Bearer())
                    .setHeader("Caller-Id", "1.2.246.562.10.00000000001.cas-oppija")
                    .setHeader("CSRF", "CSRF")
                    .setHeader("Cookie", "CSRF=CSRF")
                    .build();
            var client = HttpClient.newBuilder().build();
            var response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 404 || response.statusCode() == 204) {
                throw new RestClientException(request.uri().toString());
            } else if (response.statusCode() != 401 && (response.statusCode() < 200 || response.statusCode() > 299)) {
                LOGGER.error("request failed (HTTP " + response.statusCode() + ") with body: " + response.body());
                throw new RestClientException(request.uri().toString());
            }
            return response;
        } catch (IOException|InterruptedException e) {
            LOGGER.error("error while executing request", e);
            throw new RestClientException("error while executing request", e);
        }
    }

    public HttpResponse<String> executeRequest(HttpRequest.Builder requestBuilder) throws RestClientException {
        HttpResponse<String> res = execute(requestBuilder);
        if (res.statusCode() == 401) {
            LOGGER.info("received WWW-authenticate header: " + res.headers().firstValue("WWW-Authenticate"));
            var authHeader = res.headers().firstValue("WWW-Authenticate");
            if (authHeader.orElse("").contains("invalid_token")) {
                oauth2BearerClient.evictOauth2Bearer();
                return execute(requestBuilder);
            }
        }
        return res;
    }
}
