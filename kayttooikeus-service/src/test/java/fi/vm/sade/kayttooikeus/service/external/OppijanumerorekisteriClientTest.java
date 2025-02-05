package fi.vm.sade.kayttooikeus.service.external;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import fi.vm.sade.javautils.http.exceptions.UnhandledHttpStatusCodeException;
import fi.vm.sade.kayttooikeus.service.impl.KayttoOikeusServiceImpl;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloPerustietoDto;
import fi.vm.sade.oppijanumerorekisteri.dto.YhteystietoDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static fi.vm.sade.oppijanumerorekisteri.dto.YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OppijanumerorekisteriClientTest extends AbstractClientTest {
    @Autowired
    private OppijanumerorekisteriClient client;

    @Test
    public void getHenkilonPerustiedotTest() throws Exception {
        casAuthenticated("1.2.3.4.5");
        stubFor(post(urlEqualTo("/oppijanumerorekisteri-service/henkilo/henkiloPerustietosByHenkiloOidList"))
                .withRequestBody(equalToJson("[\"1.2.3.4.5\"]"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody(jsonResource("/henkilo/henkilonPerustiedot.json")))
        );
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
        stubFor(post(urlEqualTo("/oppijanumerorekisteri-service/s2s/duplicateHenkilos"))
                .withRequestBody(equalToJson("{\"henkiloOids\":[\"1.2.3\"]}"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody("[{\"masterOid\":\"1.2.3\",\"henkiloOid\":\"2.3.4\"},{\"masterOid\":\"1.2.3\",\"henkiloOid\":\"3.4.5\"}]")));
        Set<String> allOids = this.client.getAllOidsForSamePerson("1.2.3");
        assertEquals(3, allOids.size());
        assertTrue(allOids.containsAll(asList("1.2.3", "2.3.4", "3.4.5")));
    }

    @Test
    public void getAllOidsForSamePersonNoDuplicatesTest() {
        casAuthenticated("test");
        stubFor(post(urlEqualTo("/oppijanumerorekisteri-service/s2s/duplicateHenkilos"))
                .withRequestBody(equalToJson("{\"henkiloOids\":[\"1.2.3\"]}"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody("[]")));
        Set<String> allOids = this.client.getAllOidsForSamePerson("1.2.3");
        assertEquals(1, allOids.size());
        assertTrue(allOids.containsAll(singletonList("1.2.3")));
    }

    @Test
    public void getHenkiloByOid() {
        casAuthenticated("test");
        stubFor(get(urlEqualTo("/oppijanumerorekisteri-service/henkilo/1.2.3.4.5"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody(jsonResource("/henkilo/henkiloDto.json"))));
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

    @Test
    public void findHenkiloByOid() {
        casAuthenticated("test");
        stubFor(get(urlEqualTo("/oppijanumerorekisteri-service/henkilo/1.2.3.4.5"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody(jsonResource("/henkilo/henkiloDto.json"))));
        assertThat(this.client.findHenkiloByOid("1.2.3.4.5")).map(HenkiloDto::getOidHenkilo).hasValue("1.2.3.4.5");
    }

    @Test
    public void findHenkiloByOidWithNotFound() {
        casAuthenticated("test");
        stubFor(get(urlEqualTo("/oppijanumerorekisteri-service/henkilo/1.2.3.4.5"))
                .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody("{}")));
        assertThat(this.client.findHenkiloByOid("1.2.3.4.5")).isNotPresent();
    }

    @Test
    public void getHenkiloByHetuWithOkResponse() {
        casAuthenticated("test");
        stubFor(get(urlEqualTo("/oppijanumerorekisteri-service/henkilo/hetu=160198-943U"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody(jsonResource("/henkilo/henkiloDto.json"))));

        Optional<HenkiloDto> henkiloByHetu = client.getHenkiloByHetu("160198-943U");

        assertThat(henkiloByHetu).map(HenkiloDto::getOidHenkilo).hasValue("1.2.3.4.5");
    }

    @Test
    public void getHenkiloByHetuWithNotFoundResponse() {
        casAuthenticated("test");

        stubFor(get(urlEqualTo("/oppijanumerorekisteri-service/henkilo/hetu=160198-943U"))
                .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

        Optional<HenkiloDto> henkiloByHetu = client.getHenkiloByHetu("160198-943U");

        assertThat(henkiloByHetu).isEmpty();
    }

    @Test
    public void getHenkiloByHetuWithUnexceptedResponse() {
        casAuthenticated("test");
        stubFor(get(urlEqualTo("/oppijanumerorekisteri-service/henkilo/hetu=160198-943U"))
                .willReturn(aResponse().withStatus(HttpStatus.BAD_GATEWAY.value())));

        Throwable henkiloByHetu = catchThrowable(() -> client.getHenkiloByHetu("160198-943U"));

        assertThat(henkiloByHetu).isInstanceOf(UnhandledHttpStatusCodeException.class);
    }

    @Test
    public void resolveLanguageCodeForCurrentUserHandlesErrors() {
        assertThat(client.resolveLanguageCodeForCurrentUser()).isEqualTo(KayttoOikeusServiceImpl.FI);
    }
}
