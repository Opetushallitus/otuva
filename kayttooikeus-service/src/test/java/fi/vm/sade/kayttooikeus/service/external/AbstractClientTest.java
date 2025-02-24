package fi.vm.sade.kayttooikeus.service.external;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import fi.vm.sade.kayttooikeus.config.properties.UrlConfiguration;
import fi.vm.sade.properties.OphProperties;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.client.authentication.AttributePrincipal;
import org.apereo.cas.client.validation.AssertionImpl;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@WireMockTest(httpPort = 18080)
@SpringBootTest
@Slf4j
public abstract class AbstractClientTest {
    protected final static String WIREMOCK_HOST = "http://localhost:18080";

    @Autowired private UrlConfiguration urlConfiguration;
    @Autowired private OphProperties properties;

    @BeforeEach
    public void before() {
        log.info("Setting WireMock host to config: {}", WIREMOCK_HOST);
        urlConfiguration.addOverride("url-virkailija", WIREMOCK_HOST);
        properties.addOverride("url-virkailija", WIREMOCK_HOST);

        // Default to connection reset by peer if not matching any stub. Some tests just check the response is 404 which
        // is the default behaviour but we want to make sure the tests hit actual stub we have setup.
        stubFor(any(anyUrl()).atPriority(Integer.MAX_VALUE).willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
    }

    protected void casAuthenticated(String henkiloOid) {
        SecurityContextHolder.getContext().setAuthentication(new CasAuthenticationToken("KEY", henkiloOid, "CRED",
                Collections.emptyList(), new User(henkiloOid, "", Collections.emptyList()), new AssertionImpl(new AttributePrincipal() {
            @Override
            public String getProxyTicketFor(String service) {
                return "TICKET";
            }

            @Override
            public Map<String, Object> getAttributes() {
                return new HashMap<>();
            }

            @Override
            public String getName() {
                return henkiloOid;
            }
        })));

        stubFor(post(urlEqualTo("/cas/v1/tickets"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Location", "/TICKET")));
        stubFor(post(urlMatching("/cas/v1/tickets/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("TICKET")));
    }

    protected String jsonResource(String classpathResource) {
        try {
            return new String(getClass().getResourceAsStream(classpathResource).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not load resource: " + classpathResource + ", cause: " + e.getMessage(), e);
        }
    }

}
