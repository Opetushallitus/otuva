package fi.vm.sade.kayttooikeus.service.external;

import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.jadler.Jadler.onRequest;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class OrganisaatioClientTest extends AbstractClientTest {
    @Autowired
    private OrganisaatioClient client;
    
    @Test
    public void listOganisaatioPerustiedotTest() throws Exception {
        onRequest().havingMethod(is("GET"))
                .havingPath(is("/organisaatio-service/rest/organisaatio/hae"))
                .respond().withStatus(OK).withContentType(MediaType.APPLICATION_JSON_UTF8.getType())
                .withBody(jsonResource("classpath:organisaatio/organisaatioServiceHaeResponse.json"));
        List<OrganisaatioPerustieto> results = this.client.listOganisaatioPerustiedotRecusive(singletonList("1.2.246.562.10.14175756379"));
        assertEquals(1, results.size());
        assertEquals("1.2.246.562.10.14175756379", results.get(0).getOid());
    }
}
