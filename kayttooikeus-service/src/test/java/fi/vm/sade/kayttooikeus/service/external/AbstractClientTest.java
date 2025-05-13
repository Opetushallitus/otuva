package fi.vm.sade.kayttooikeus.service.external;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WireMockTest(httpPort = 18080)
@SpringBootTest
@TestPropertySource(properties = {
    "url-virkailija=" + AbstractClientTest.WIREMOCK_HOST,
    "url-varda=" + AbstractClientTest.WIREMOCK_HOST
})
@ContextConfiguration
@Slf4j
public abstract class AbstractClientTest {
    protected final static String WIREMOCK_HOST = "http://localhost:18080";

    @BeforeEach
    public void before() {
        log.info("Setting WireMock host to config: {}", WIREMOCK_HOST);

        // Default to connection reset by peer if not matching any stub. Some tests just check the response is 404 which
        // is the default behaviour but we want to make sure the tests hit actual stub we have setup.
        stubFor(any(anyUrl()).atPriority(Integer.MAX_VALUE).willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        // Oauth2 bearer token for authenticated requests
        stubFor(post(urlEqualTo("/oauth2/token"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody("{ \"access_token\": \"token\", \"expires_in\": 12346, \"token_type\": \"Bear\" }")));
    }

    protected String jsonResource(String classpathResource) {
        try {
            return new String(getClass().getResourceAsStream(classpathResource).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not load resource: " + classpathResource + ", cause: " + e.getMessage(), e);
        }
    }

}
