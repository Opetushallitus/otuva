package fi.vm.sade.kayttooikeus.service.external;

import fi.vm.sade.ryhmasahkoposti.api.dto.EmailData;
import org.apache.http.HttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import static net.jadler.Jadler.onRequest;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
public class RyhmasahkopostiClientTest extends AbstractClientTest {
    @Autowired
    private RyhmasahkopostiClient client;

    @Test
    public void sendRyhmasahkopostiTest() throws Exception {
        casAuthenticated("test");
        onRequest().havingPath(is("/ryhmasahkoposti-service/j_spring_cas_security_check"))
                .respond().withStatus(OK).withBody("TICKET");
        onRequest().havingMethod(is("POST"))
                .havingPath(is("/ryhmasahkoposti-service/email"))
                .respond().withStatus(OK).withContentType(MediaType.APPLICATION_JSON_UTF8.getType())
                .withBody("12345");
        HttpResponse results = this.client.sendRyhmasahkoposti(new EmailData());
        assertNotNull(results);
        assertEquals(200, results.getStatusLine().getStatusCode());
    }
}
