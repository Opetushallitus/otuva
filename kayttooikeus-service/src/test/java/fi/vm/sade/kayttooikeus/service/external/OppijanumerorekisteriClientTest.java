package fi.vm.sade.kayttooikeus.service.external;



import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloPerustietoDto;
import fi.vm.sade.oppijanumerorekisteri.dto.YhteystietoDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static fi.vm.sade.oppijanumerorekisteri.dto.YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.jadler.Jadler.onRequest;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class OppijanumerorekisteriClientTest extends AbstractClientTest {
    @Autowired
    private OppijanumerorekisteriClient client;
    
    @Test
    public void getHenkilonPerustiedotTest() throws Exception {
        casAuthenticated("1.2.3.4.5");
        onRequest().havingMethod(is("POST"))
                .havingPath(is("/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloOidList"))
                .havingBodyEqualTo("[\"1.2.3.4.5\"]")
                .respond().withStatus(OK).withContentType(MediaType.APPLICATION_JSON_UTF8.getType())
                .withBody(jsonResource("classpath:henkilo/henkilonPerustiedot.json"));
        List<HenkiloPerustietoDto> results = this.client.getHenkilonPerustiedot(singletonList("1.2.3.4.5"));
        assertEquals(1, results.size());
        assertEquals("1.2.3.4.5", results.get(0).getOidHenkilo());
        assertEquals("EN", results.get(0).getAsiointiKieli().getKieliKoodi());

        Optional<HenkiloPerustietoDto> result = this.client.getHenkilonPerustiedot("1.2.3.4.5");
        assertTrue(result.isPresent());
        assertEquals("1.2.3.4.5", result.get().getOidHenkilo());
    }

    @Test
    public void getAllOidsForSamePersonTest() {
        casAuthenticated("test");
        onRequest().havingMethod(is("POST"))
                .havingPath(is("/oppijanumerorekisteri-service/s2s/duplicateHenkilos"))
                .havingBody(equalToIgnoringWhiteSpace("{\"henkiloOids\":[\"1.2.3\"]}"))
                .respond().withStatus(OK).withContentType(MediaType.APPLICATION_JSON_UTF8.getType())
                .withBody("[\n" +
                        "  {\n" +
                        "    \"masterOid\": \"1.2.3\",\n" +
                        "    \"henkiloOid\": \"2.3.4\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"masterOid\": \"1.2.3\",\n" +
                        "    \"henkiloOid\": \"3.4.5\"\n" +
                        "  }\n" +
                        "]");
        Set<String> allOids = this.client.getAllOidsForSamePerson("1.2.3");
        assertEquals(3, allOids.size());
        assertTrue(allOids.containsAll(asList("1.2.3", "2.3.4", "3.4.5")));
    }

    @Test
    public void getAllOidsForSamePersonNoDuplicatesTest() {
        casAuthenticated("test");
        onRequest().havingMethod(is("POST"))
                .havingPath(is("/oppijanumerorekisteri-service/s2s/duplicateHenkilos"))
                .havingBody(equalToIgnoringWhiteSpace("{\"henkiloOids\":[\"1.2.3\"]}"))
                .respond().withStatus(OK).withContentType(MediaType.APPLICATION_JSON_UTF8.getType())
                .withBody("[]");
        Set<String> allOids = this.client.getAllOidsForSamePerson("1.2.3");
        assertEquals(1, allOids.size());
        assertTrue(allOids.containsAll(singletonList("1.2.3")));
    }

    @Test
    public void getHenkiloByOid() {
        casAuthenticated("test");
        onRequest().havingMethod(is("GET"))
                .havingPath(is("/oppijanumerorekisteri-service/henkilo/1.2.3.4.5"))
                .respond().withStatus(OK).withContentType(MediaType.APPLICATION_JSON_UTF8.getType())
                .withBody(jsonResource("classpath:henkilo/henkiloDto.json"));
        HenkiloDto henkiloDto = this.client.getHenkiloByOid("1.2.3.4.5");
        assertEquals("1.2.3.4.5", henkiloDto.getOidHenkilo());
        assertEquals("etunimi", henkiloDto.getEtunimet());
        assertEquals("etunimi", henkiloDto.getKutsumanimi());
        assertEquals("sukunimi", henkiloDto.getSukunimi());
        assertEquals(1, henkiloDto.getYhteystiedotRyhma().size());
        assertEquals("yhteystietotyyppi2email@emai.fi", henkiloDto.getYhteystiedotRyhma().iterator().next().getYhteystieto()
                .stream().filter(yhteystietoDto -> yhteystietoDto.getYhteystietoTyyppi().equals(YHTEYSTIETO_SAHKOPOSTI))
                .findFirst().orElse(new YhteystietoDto()).getYhteystietoArvo());
    }
}
