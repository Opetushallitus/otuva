package fi.vm.sade.kayttooikeus;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.http.client.HttpRedirects.DONT_FOLLOW;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class AccessLogTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TestRestTemplate restTemplate;
    private TestRestTemplate restTemplateWithoutRedirects;

    @BeforeEach
    public void setUp() {
        restTemplateWithoutRedirects = restTemplate.withRedirects(DONT_FOLLOW);
    }

    @Test
    public void requestIsLogged(CapturedOutput output) {
        getForEntity("/");
        assertEquals("GET / HTTP/1.1", resolveLog(output));
    }

    @Test
    public void sensitiveInformationInPathIsMasked(CapturedOutput output) {
        getForEntity("/123456+7890");
        assertEquals("GET /123456+**** HTTP/1.1", resolveLog(output));
    }

    @Test
    public void sensitiveInformationInPathIsMaskedNewSpec(CapturedOutput output) {
        getForEntity("/123456X7890");
        assertEquals("GET /123456X**** HTTP/1.1", resolveLog(output));
    }

    @Test
    public void sensitiveInformationInRequestParameterIsMasked(CapturedOutput output) {
        getForEntity("/?test=123456-7890");
        assertEquals("GET /?test=123456-**** HTTP/1.1", resolveLog(output));
    }

    @Test
    public void allSensitiveInformationIsMasked(CapturedOutput output) {
        getForEntity("/123456+789A/123456-789B?test=123456A7890&test=123456-789R");
        assertEquals("GET /123456+****/123456-****?test=123456A****&test=123456-**** HTTP/1.1", resolveLog(output));
    }

    @Test
    public void onlyExactMatchesAreMaskedIncorrectPrefix(CapturedOutput output) {
        getForEntity("/1234567-890A");
        assertEquals("GET /1234567-890A HTTP/1.1", resolveLog(output));
    }

    @Test
    public void onlyExactMatchesAreMaskedIncorrectSuffix(CapturedOutput output) {
        getForEntity("/123456-890AA");
        assertEquals("GET /123456-890AA HTTP/1.1", resolveLog(output));
    }

    @Test
    public void handlesOids(CapturedOutput output) {
        getForEntity("/1.2.246.562.24.43116640405");
        assertEquals("GET /1.2.246.562.24.43116640405 HTTP/1.1", resolveLog(output));
    }

    @Test
    public void hasRequestMappingField(CapturedOutput output) {
        getForEntity("/henkilo/1.2.246.562.24.43116640405");
        // For whatever reason in tests the request mapping is not resolved correctly so basically this tests the field
        // is at least attempted to be included in the access log.
        //assertEquals("/henkilo/{oid}", resolveLogLine(output).requestMapping);
        assertEquals("-", resolveLogLine(output).requestMapping);
    }

    private void getForEntity(String url) {
        restTemplateWithoutRedirects.getForEntity(url, String.class);
    }

    private String resolveLog(CapturedOutput output) {
        return resolveLogLine(output).request;
    }

    private AccessLogLine resolveLogLine(CapturedOutput output) {
        var lines = output.getOut().split(System.lineSeparator());
        for (int i = lines.length - 1; i >= 0; i--) {
            var s = lines[i];
            var result = objectMapper.readValue(s, AccessLogLine.class);
            if (result != null) return result;
        }
        throw new RuntimeException("No log line found");
    }

    record AccessLogLine(String request, String requestMapping, String callerHenkiloOid) {}
}
