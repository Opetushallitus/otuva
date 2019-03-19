package fi.vm.sade.cas.oppija.surrogate;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SurrogatePropertiesTest {

    @Test
    public void getCredentials() {
        SurrogateProperties properties = SurrogateProperties.ofCredentials("ed4b7ae7", "3ba56df8-88b8-4805-9b04-2f8e7a61", "abc-api-key");

        String credentials = properties.getCredentials();

        assertEquals("ZWQ0YjdhZTc6YWJjLWFwaS1rZXk=", credentials);
    }

    @Test
    public void getChecksum() {
        SurrogateProperties properties = SurrogateProperties.ofCredentials("ed4b7ae7", "3ba56df8-88b8-4805-9b04-2f8e7a61", "abc-api-key");
        String path = "/service/hpa/user/register/ed4b7ae7/080297-915A?requestId=02fd35dc-99e6-477b-b6e2-03f02cbf3666";
        Instant instant = Instant.parse("2017-02-09T10:29:42.09Z");

        String checksum = properties.getChecksum(path, instant);

        assertEquals("ed4b7ae7 2017-02-09T10:29:42.09Z z7X+xWtrvth1L7Ql6B/4xZ0iQ1VjToWX4TnHVLo8RGo=", checksum);
    }

}
