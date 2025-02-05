package fi.vm.sade.kayttooikeus.service.external;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import fi.vm.sade.kayttooikeus.config.properties.UrlConfiguration;
import fi.vm.sade.properties.OphProperties;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.client.authentication.AttributePrincipal;
import org.apereo.cas.client.validation.AssertionImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public abstract class AbstractClientTest {
    protected final static String WIREMOCK_HOST = "http://localhost:18080";

    @Autowired private UrlConfiguration urlConfiguration;
    @Autowired private OphProperties properties;

    protected WireMockServer wm;

    @Before
    public void before() {
        log.info("Setting WireMock host to config: {}", WIREMOCK_HOST);
        urlConfiguration.addOverride("url-virkailija", WIREMOCK_HOST);
        properties.addOverride("url-virkailija", WIREMOCK_HOST);

        var uri = URI.create(WIREMOCK_HOST);

        configureFor(uri.getHost(), uri.getPort());
        wm = new WireMockServer(uri.getPort());
        wm.start();

        // Default to connection reset by peer if not matching any stub. Some tests just check the response is 404 which
        // is the default behaviour but we want to make sure the tests hit actual stub we have setup.
        stubFor(any(anyUrl()).atPriority(Integer.MAX_VALUE).willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
    }

    @After
    public void after() {
        wm.stop();
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
