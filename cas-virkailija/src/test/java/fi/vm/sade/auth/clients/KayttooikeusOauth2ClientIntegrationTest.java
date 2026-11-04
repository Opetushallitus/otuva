package fi.vm.sade.auth.clients;

import com.sun.net.httpserver.HttpServer;
import fi.vm.sade.auth.cas.CasUserAttributes;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KayttooikeusOauth2ClientIntegrationTest {
    private HttpServer server;
    private AtomicReference<String> requestBody;
    private KayttooikeusOauth2Client kayttooikeusClient;

    @SneakyThrows
    @BeforeEach
    void setUp() throws IOException {
        requestBody = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/cas/auth", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = """
                    {"oidHenkilo":"1.2.3","username":"test-user","sessionindex":[],
                     "mfaProvider":null,"kayttajaTyyppi":"VIRKAILIJA","idpEntityId":null,"roles":[]}
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();

        var bearerClient = mock(Oauth2BearerClient.class);
        when(bearerClient.getOauth2Bearer()).thenReturn("test-bearer");
        kayttooikeusClient = new KayttooikeusOauth2Client(new Oauth2Client(bearerClient));
        ReflectionTestUtils.setField(kayttooikeusClient, "baseurl",
                "http://127.0.0.1:" + server.getAddress().getPort() + "/");
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void sendsPercentAndQuestionMarkInPasswordAsJsonWithoutFormEncoding() {
        assertPasswordIsSentUnchanged("start%middle?end");
    }

    @Test
    void sendsFormEncodedPercentAndQuestionMarkAsLiteralPasswordCharacters() {
        // The client accepts a password, not a form-encoded value. It does not URL-decode it.
        assertPasswordIsSentUnchanged("start%25middle%3Fend");
    }

    private void assertPasswordIsSentUnchanged(String password) {
        Optional<CasUserAttributes> result = kayttooikeusClient
                .getUserAttributesByUsernamePassword("test-user", password);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().username()).isEqualTo("test-user");
        assertThat(requestBody.get()).isEqualTo(
                "{\"username\":\"test-user\",\"password\":\"" + password + "\"}");
    }
}
