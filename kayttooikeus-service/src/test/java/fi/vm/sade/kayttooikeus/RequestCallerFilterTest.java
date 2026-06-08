package fi.vm.sade.kayttooikeus;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
  RSAPrivateKey privateKey;

  @LocalServerPort
  private int port;

  private final String oauth2ClientOid = "1.2.246.562.24.43006465835";

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
}
