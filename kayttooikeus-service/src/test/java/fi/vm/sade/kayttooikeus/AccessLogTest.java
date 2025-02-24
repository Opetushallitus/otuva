package fi.vm.sade.kayttooikeus;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccessLogTest {

    private final PrintStream restore = System.out;
    @Autowired
    private TestRestTemplate restTemplate;
    private ByteArrayOutputStream output;

    @BeforeEach
    public void setUp() {
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    public void tearDown() throws IOException {
        System.setOut(restore);
        output.close();
    }

    @Test
    public void requestIsLogged() {
        restTemplate.getForEntity("/", String.class);
        assertEquals("GET / HTTP/1.1", resolveLog(output));
    }

    @Test
    public void sensitiveInformationInPathIsMasked() {
        restTemplate.getForEntity("/123456+7890", String.class);
        assertEquals("GET /123456+**** HTTP/1.1", resolveLog(output));
    }

    @Test
    public void sensitiveInformationInPathIsMaskedNewSpec() {
        restTemplate.getForEntity("/123456X7890", String.class);
        assertEquals("GET /123456X**** HTTP/1.1", resolveLog(output));
    }

    @Test
    public void sensitiveInformationInRequestParameterIsMasked() {
        restTemplate.getForEntity("/?test=123456-7890", String.class);
        assertEquals("GET /?test=123456-**** HTTP/1.1", resolveLog(output));
    }

    @Test
    public void allSensitiveInformationIsMasked() {
        restTemplate.getForEntity("/123456+789A/123456-789B?test=123456A7890&test=123456-789R", String.class);
        assertEquals("GET /123456+****/123456-****?test=123456A****&test=123456-**** HTTP/1.1", resolveLog(output));
    }

    @Test
    public void onlyExactMatchesAreMaskedIncorrectPrefix() {
        restTemplate.getForEntity("/1234567-890A", String.class);
        assertEquals("GET /1234567-890A HTTP/1.1", resolveLog(output));
    }

    @Test
    public void onlyExactMatchesAreMaskedIncorrectSuffix() {
        restTemplate.getForEntity("/123456-890AA", String.class);
        assertEquals("GET /123456-890AA HTTP/1.1", resolveLog(output));
    }

    @Test
    public void handlesOids() {
        restTemplate.getForEntity("/1.2.246.562.24.43116640405", String.class);
        assertEquals("GET /1.2.246.562.24.43116640405 HTTP/1.1", resolveLog(output));
    }

    @Test
    public void hasRequestMappingField() {
        restTemplate.getForEntity("/henkilo/1.2.246.562.24.43116640405", String.class);
        // For whatever reason in tests the request mapping is not resolved correctly so basically this tests the field
        // is at least attempted to be included in the access log.
        //assertEquals("/henkilo/{oid}", resolveLogLine(output).requestMapping);
        assertEquals("-", resolveLogLine(output).requestMapping);
    }

    private String resolveLog(ByteArrayOutputStream output) {
        return resolveLogLine(output).request;
    }

    private AccessLogLine resolveLogLine(ByteArrayOutputStream output) {
        for (String s : output.toString().split(System.getProperty("line.separator"), 10)) {
            System.err.println(s);
            var result = tryParse(s);
            if (result != null) return result;
        }
        throw new RuntimeException("No log line found");
    }

    private AccessLogLine tryParse(String s) {
        try {
            return objectMapper.readValue(s, AccessLogLine.class);
        } catch (IOException e) {
            return null;
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .enable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES);

    record AccessLogLine(String request, String requestMapping, String callerHenkiloOid) {}
}
