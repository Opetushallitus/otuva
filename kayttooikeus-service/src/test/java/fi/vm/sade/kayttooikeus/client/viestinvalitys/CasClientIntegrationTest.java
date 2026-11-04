package fi.vm.sade.kayttooikeus.client.viestinvalitys;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CasClientIntegrationTest {
    private HttpServer server;
    private AtomicReference<String> ticketRequestBody;
    private CasClient casClient;

    @BeforeEach
    void setUp() throws IOException {
        ticketRequestBody = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/tickets", exchange -> {
            if (exchange.getRequestURI().getPath().equals("/v1/tickets")) {
                ticketRequestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                exchange.getResponseHeaders().set("Location", "/v1/tickets/TGT-test");
                exchange.sendResponseHeaders(201, -1);
            } else {
                byte[] response = "ST-test".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, response.length);
                try (var output = exchange.getResponseBody()) {
                    output.write(response);
                }
            }
            exchange.close();
        });
        server.start();

        casClient = new CasClient(
                HttpClient.newHttpClient(),
                "http://127.0.0.1:" + server.getAddress().getPort(),
                "test-user",
                "start%middle?end");
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void sendsPercentAndQuestionMarkInPasswordAsEncodedFormParameters() throws Exception {
        assertThat(casClient.getTicket("http://service.example")).isEqualTo("ST-test");

        assertThat(decodeForm(ticketRequestBody.get()))
                .containsEntry("username", "test-user")
                .containsEntry("password", "start%middle?end");
        assertThat(ticketRequestBody.get()).contains("password=start%25middle%3Fend");
    }

    private Map<String, String> decodeForm(String body) {
        return Arrays.stream(body.split("&"))
                .map(parameter -> parameter.split("=", 2))
                .collect(Collectors.toMap(
                        parameter -> URLDecoder.decode(parameter[0], StandardCharsets.UTF_8),
                        parameter -> URLDecoder.decode(parameter[1], StandardCharsets.UTF_8)));
    }
}
