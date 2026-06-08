package fi.vm.sade.kayttooikeus;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Sql("/truncate_tables.sql")
@Sql("/oauth2-client.sql")
@Transactional
@WireMockTest(httpPort = 18080)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(OutputCaptureExtension.class)
public class RequestCallerFilterTest {

  @Value("${kayttooikeus.oauth2.privatekey}")
  private RSAPrivateKey privateKey;

  @LocalServerPort
  private int port;

  private final String oauth2ClientOid = "1.2.246.562.24.43006465835";

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("cas.oppija.url", () -> "http://localhost:18080/cas-oppija");
    registry.add("cas.oppija.login", () -> "http://localhost:18080/cas-oppija/login");
    registry.add("cas.url", () -> "http://localhost:18080/cas");
    registry.add("cas.login", () -> "http://localhost:18080/cas/login");
  }

  @Test
  public void logsCallerHenkiloOidWhenCallerAuthenticatedWithOauth2(CapturedOutput output)
          throws Exception {
    var token = generateToken(oauth2ClientOid);

    var client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

    var request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/s2s/canUserAccessUser"))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofString());

    System.out.println("response: " + response);

    assertThat(output).contains("\"callerHenkiloOid\": \"1.2.246.562.24.43006465835\"");
  }

  @Test
  public void logsCallerHenkiloOidWhenCallerAuthenticatedWithCasVirkailija(CapturedOutput output)
          throws Exception {
    var cookie = getCookie("/cas-virkailija");
    var ticket = "ST-30-JVB-gESc2Yc3S-zV25JOHbVEeBo-ip-10-0-55-20";
    // Stub cas-virkailija endpoints
    stubFor(
            get(urlEqualTo(
                    "/cas/login?service="
                            + URLEncoder.encode(
                            "http://localhost:" + port + "/kayttooikeus-service", StandardCharsets.UTF_8)))
                    .willReturn(
                            aResponse()
                                    .withStatus(302)
                                    .withHeader(
                                            "Location",
                                            "http://localhost:" + port + "/kayttooikeus-service/j_spring_cas_security_check?ticket="
                                                    + ticket)
                                    .withHeader("Set-Cookie", cookie.toString())));
    stubFor(
            post(urlEqualTo(
                    "/cas/login?service="
                            + URLEncoder.encode(
                            "http://localhost:" + port + "/kayttooikeus-service/j_spring_cas_security_check",
                            StandardCharsets.UTF_8)))
                    .willReturn(
                            aResponse()
                                    .withStatus(302)
                                    .withHeader(
                                            "Location",
                                            "http://localhost:" + port + "/kayttooikeus-service/j_spring_cas_security_check?ticket="
                                                    + ticket)
                                    .withHeader("Set-Cookie", cookie.toString())));
    stubFor(
            get(urlEqualTo(
                    "/cas/login?service="
                            + URLEncoder.encode(
                            "http://localhost:" + port + "/kayttooikeus-service/j_spring_cas_security_check",
                            StandardCharsets.UTF_8)))
                    .willReturn(
                            aResponse()
                                    .withStatus(302)
                                    .withHeader(
                                            "Location",
                                            "http://localhost:" + port + "/kayttooikeus-service/j_spring_cas_security_check?ticket="
                                                    + ticket)
                                    .withHeader("Set-Cookie", cookie.toString())));
    // validate ticket, provide cas response
    var urlEncodedServiceParam = URLEncoder.encode(
            "http://localhost:" + port + "/kayttooikeus-service/j_spring_cas_security_check",
            StandardCharsets.UTF_8);
    var casValidationUrl = "/cas/p3/proxyValidate?ticket={ticket}&service={service}".replace("{ticket}", ticket).replace("{service}", urlEncodedServiceParam);
    stubFor(
            get(urlEqualTo(casValidationUrl))
                    .willReturn(
                            aResponse()
                                    .withStatus(200)
                                    .withBody(readResource("/cas-virkailija-auth-response.xml"))));

    var client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

    var loginRequest =
            HttpRequest.newBuilder()
                    .uri(
                            URI.create(
                                    "http://localhost:18080/cas/login?service="
                                                    + URLEncoder.encode(
                                                    "http://localhost:" + port + "/kayttooikeus-service/j_spring_cas_security_check",
                                                    StandardCharsets.UTF_8)))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

    var loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());

    var responseHeaders = loginResponse.headers();

    var cookies = responseHeaders.allValues("Set-Cookie").get(0);

    var request =
            HttpRequest.newBuilder()
                    .header("Cookie", cookies)
                    .uri(URI.create("http://localhost:" + port + "/kayttooikeus-service/henkilo/1.2.3.4.5/kayttajatiedot"))
                    .GET()
                    .build();

    client.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(output).contains("\"callerHenkiloOid\": \"1.2.246.562.98.1234567890\"");
  }

  @Test
  public void doesNotLogCallerHenkiloOidWhenCallerAuthenticatedWithCasOppija(CapturedOutput output)
          throws IOException, InterruptedException {
    var ticket = "ST-30-JVB-gESc2Yc3S-zV25JOHbVEeBo-ip-10-0-55-20";
    var cookie = getCookie("/cas-oppija");
    // Stub cas-oppija endpoints
    stubFor(
      get(urlEqualTo(
    "/cas-oppija/login?service="
            + URLEncoder.encode(
            "http://localhost:" + port + "/kayttooikeus-service", StandardCharsets.UTF_8)))
        .willReturn(
            aResponse()
                .withStatus(302)
                .withHeader(
                        "Location",
                        "http://localhost:" + port + "/kayttooikeus-service/j_spring_cas_security_check?ticket="
                                + ticket)
                .withHeader("Set-Cookie", cookie.toString())));
    stubFor(
        post(urlEqualTo(
            "/cas-oppija/login?service="
                + URLEncoder.encode(
                "http://localhost:" + port + "/kayttooikeus-service/j_spring_cas_security_check",
                StandardCharsets.UTF_8)))
            .willReturn(
                aResponse()
                    .withStatus(302)
                    .withHeader(
                            "Location",
                            "http://localhost:" + port + "/kayttooikeus-service/j_spring_cas_security_check?ticket="
                                    + ticket)
                    .withHeader("Set-Cookie", cookie.toString())));
    stubFor(
        get(urlEqualTo(
            "/cas-oppija/login?service="
                + URLEncoder.encode(
                "http://localhost:" + port + "/kayttooikeus-service/j_spring_cas_security_check",
                StandardCharsets.UTF_8)))
            .willReturn(
                aResponse()
                    .withStatus(302)
                    .withHeader(
                            "Location",
                            "http://localhost:" + port + "/kayttooikeus-service/j_spring_cas_security_check?ticket="
                                    + ticket)
                    .withHeader("Set-Cookie", cookie.toString())));
    // validate ticket, provide cas response
    stubFor(
      get(urlEqualTo(
        "/cas/p3/proxyValidate?ticket=%s&service=%s"
            .formatted(
                ticket,
                URLEncoder.encode(
                    "http://localhost:" + port + "/kayttooikeus-service/j_spring_cas_security_check",
                    StandardCharsets.UTF_8))))
        .willReturn(
            aResponse()
                .withStatus(200)
                .withBody(readResource("/cas-oppija-auth-response.xml"))));

    var client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

    var loginRequest =
            HttpRequest.newBuilder()
              .uri(
                URI.create(
                  "http://localhost:18080/cas-oppija/login?service="
                                + URLEncoder.encode(
                                "http://localhost:" + port +"/kayttooikeus-service/j_spring_cas_security_check",
                                StandardCharsets.UTF_8)))
              .POST(HttpRequest.BodyPublishers.noBody())
              .build();

    var loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());

    var responseHeaders = loginResponse.headers();

    var cookies = responseHeaders.allValues("Set-Cookie").get(0);

    var request =
            HttpRequest.newBuilder()
                    .header("Cookie", cookies)
                    .uri(URI.create("http://localhost:" + port + "/henkilo/current/omattiedot"))
                    .GET()
                    .build();

    client.send(request, HttpResponse.BodyHandlers.ofString());

    // callerHenkiloOid is not logged for cas-oppija cases since RequestCallerFilter expects
    // authenticated user's user details to be of type
    // OpintopolkuUserDetailsService.OpintopolkuUserDetailsl, which expects oid to be in field
    // oidHenkilo, not personOid
    assertThat(output).doesNotContain("\"callerHenkiloOid\": \"1.2.246.562.98.19783284870\"");
    assertThat(output).contains("\"callerHenkiloOid\": \"-\"");
  }

  private Cookie getCookie(String path) {
    var tgc = "TGC=asd";
    return new Cookie(
            path, tgc, "SameSite=none", "SameSite=None", "Secure", "HttpOnly", "Path=" + path);
  }

  private String generateToken(String subject) throws Exception {
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .subject(subject)
            .issuer("http://localhost:" + port)
            .audience("oppijanumerorekisteri-service")
            .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
            .issueTime(Date.from(Instant.now()))
            .claim(
                    "roles",
                    Map.of(
                            "1.2.246.562.10.00000000001",
                            List.of("APP_OPPIJANUMEROREKISTERI_REKISTERINPITAJA", "APP_OPPIJANUMEROREKISTERI_REKISTERINPITAJA_1.2.246.562.10.00000000001", "APP_KAYTTOOIKEUS_REKISTERINPITAJA_1.2.246.562.10.00000000001")))
            .claim("sub", subject)
            .build();

    SignedJWT jwt = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
            claims
    );
    jwt.sign(new RSASSASigner(privateKey));
    return jwt.serialize();
  }

  private byte[] readBytes(String path) {
    try (var inputStream = getClass().getResourceAsStream(path)) {
      if (inputStream == null) {
        throw new RuntimeException("Resource not found: " + path);
      }
      return inputStream.readAllBytes();
    } catch (Exception e) {
      throw new RuntimeException("Failed to read resource: " + path, e);
    }
  }

  private String readResource(String path) {
    return new String(readBytes(path));
  }
}
