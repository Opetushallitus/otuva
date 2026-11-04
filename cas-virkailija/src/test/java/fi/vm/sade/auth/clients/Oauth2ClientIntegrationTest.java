package fi.vm.sade.auth.clients;

import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Oauth2ClientIntegrationTest {
    private HttpServer server;
    private AtomicReference<String> requestBody;
    private Oauth2Client oauth2Client;

    @SneakyThrows
    @BeforeEach
    void setUp() throws IOException {
        requestBody = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/cas/auth", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();

        var bearerClient = mock(Oauth2BearerClient.class);
        when(bearerClient.getOauth2Bearer()).thenReturn("test-bearer");
        oauth2Client = new Oauth2Client(bearerClient);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void sendsPercentAndQuestionMarkInPasswordAsJsonWithoutFormEncoding() {
        String password = "start%middle?end";
        String body = "{\"username\":\"test-user\",\"password\":\"" + password + "\"}";

        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/cas/auth"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        var response = oauth2Client.executeRequest(request);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(requestBody.get()).isEqualTo(body);
    }
}
