package fi.vm.sade.kayttooikeus.service.it;

import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.kayttooikeus.aspects.HenkiloHelper;
import fi.vm.sade.kayttooikeus.controller.KutsuPopulator;
import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.enumeration.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioCacheRepository;
import fi.vm.sade.kayttooikeus.repositories.criteria.KutsuCriteria;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkiloCreateByKutsuDto;
import fi.vm.sade.kayttooikeus.repositories.populate.*;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.*;
import fi.vm.sade.oppijanumerorekisteri.dto.KielisyysDto;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static fi.vm.sade.kayttooikeus.controller.KutsuPopulator.kutsu;
import static fi.vm.sade.kayttooikeus.model.Identification.HAKA_AUTHENTICATION_IDP;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.KutsuOrganisaatioPopulator.kutsuOrganisaatio;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloKayttoOikeusPopulator.myonnettyKayttoOikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.TextGroupPopulator.text;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@RunWith(SpringRunner.class)
public class KutsuServiceTest extends AbstractServiceIntegrationTest {
    @Autowired
    private KutsuService kutsuService;
    
    @MockBean
    private OrganisaatioClient organisaatioClient;

    @MockBean
    private RyhmasahkopostiClient ryhmasahkopostiClient;

    @SpyBean
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @MockBean
    private HenkiloHelper henkiloHelper;

    @MockBean
    private OrganisaatioCacheRepository organisaatioCacheRepository;

    @MockBean
    private LdapSynchronizationService ldapSynchronizationService;

    @Test
    @WithMockUser(username = "1.2.4", authorities = "ROLE_APP_HENKILONHALLINTA_CRUD")
    public void listAvoinKutsus() {
        Kutsu kutsu1 = populate(kutsu("Essi", "Esimerkki", "a@eaxmple.com")
                .kutsuja("1.2.3").aikaleima(LocalDateTime.of(2016,1,1,0,0,0, 0))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                        .ryhma(kayttoOikeusRyhma("RYHMA1"))
                )),
            kutsu2 = populate(kutsu("Matti", "Meikäläinen", "b@eaxmple.com")
                .kutsuja("1.2.4").aikaleima(LocalDateTime.of(2016,2,1,0,0,0,0))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                        .ryhma(kayttoOikeusRyhma("RYHMA2")))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.6")
                        .ryhma(kayttoOikeusRyhma("RYHMA3")))
            ),
            kutsu3 = populate(kutsu("Eero", "Esimerkki", "c@eaxmple.com")
                .tila(KutsunTila.POISTETTU)
                .kutsuja("1.2.4").aikaleima(LocalDateTime.of(2016,1,1,0,0,0,0))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5").ryhma(kayttoOikeusRyhma("RYHMA1"))
            ));

        OrganisaatioPerustieto org1 = new OrganisaatioPerustieto();
        org1.setOid("1.2.3.4.5");
        org1.setNimi(new TextGroupMapDto().put("fi", "Nimi2").asMap());
        OrganisaatioPerustieto org2 = new OrganisaatioPerustieto();
        org2.setOid("1.2.3.4.6");
        org2.setNimi(new TextGroupMapDto().put("fi", "Nimi1").asMap());
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.5"), Matchers.any()))
                .willReturn(Optional.of(org1));
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.6"), Matchers.any()))
                .willReturn(Optional.of(org2));
        
        List<KutsuReadDto> kutsus = kutsuService.listKutsus(KutsuOrganisaatioOrder.AIKALEIMA, Sort.Direction.ASC, new KutsuCriteria().withQuery("matti meikäläinen"), null, null);
        assertEquals(1, kutsus.size());
        assertEquals(LocalDateTime.of(2016,2,1,0,0,0,0), kutsus.get(0).getAikaleima());
        assertEquals(kutsu2.getId(), kutsus.get(0).getId());
        assertEquals("b@eaxmple.com", kutsus.get(0).getSahkoposti());
        assertEquals(2, kutsus.get(0).getOrganisaatiot().size());
        assertThat(kutsus).flatExtracting(KutsuReadDto::getOrganisaatiot)
                .extracting(KutsuReadDto.KutsuOrganisaatioDto::getOrganisaatioOid)
                .containsExactlyInAnyOrder("1.2.3.4.5", "1.2.3.4.6");
        assertThat(kutsus).extracting(KutsuReadDto::getEtunimi).containsExactlyInAnyOrder("Matti");
        assertThat(kutsus).extracting(KutsuReadDto::getSukunimi).containsExactlyInAnyOrder("Meikäläinen");
        assertThat(kutsus).flatExtracting(KutsuReadDto::getOrganisaatiot)
                .extracting(KutsuReadDto.KutsuOrganisaatioDto::getNimi)
                .extracting(TextGroupMapDto::getTexts)
                .extracting(map -> map.get("fi"))
                .containsExactlyInAnyOrder("Nimi1", "Nimi2");
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = "ROLE_APP_HENKILONHALLINTA_CRUD")
    public void createKutsuTest() {
        HttpEntity emailResponseEntity = new StringEntity("12345", Charset.forName("UTF-8"));
        BasicHttpResponse response = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "Ok"));
        response.setEntity(emailResponseEntity);
        given(ryhmasahkopostiClient.sendRyhmasahkoposti(any())).willReturn(response);
        
        OrganisaatioPerustieto org1 = new OrganisaatioPerustieto();
        org1.setOid("1.2.246.562.10.00000000001");
        org1.setNimi(new TextGroupMapDto().put("FI", "Opetushallitus").asMap());
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.246.562.10.00000000001"), Matchers.any()))
                .willReturn(Optional.of(org1));
        
        MyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo("1.2.4", "1.2.246.562.10.00000000001"),
                kayttoOikeusRyhma("kayttoOikeusRyhma").withKuvaus(text("fi", "Käyttöoikeusryhmä"))));
        
        Long kayttoOikeusRyhmaId = tapahtuma.getKayttoOikeusRyhma().getId();
        KutsuCreateDto.KayttoOikeusRyhmaDto kutsuKayttoOikeusRyhma = new KutsuCreateDto.KayttoOikeusRyhmaDto();
        kutsuKayttoOikeusRyhma.setId(kayttoOikeusRyhmaId);
        
        KutsuCreateDto kutsu = new KutsuCreateDto();
        kutsu.setEtunimi("Etu");
        kutsu.setSukunimi("Suku");
        kutsu.setSahkoposti("example@example.com");
        kutsu.setAsiointikieli(Asiointikieli.fi);
        kutsu.setOrganisaatiot(new LinkedHashSet<>());
        KutsuCreateDto.KutsuOrganisaatioDto kutsuOrganisaatio = new KutsuCreateDto.KutsuOrganisaatioDto();
        kutsuOrganisaatio.setOrganisaatioOid("1.2.246.562.10.00000000001");
        kutsuOrganisaatio.setKayttoOikeusRyhmat(Stream.of(kutsuKayttoOikeusRyhma).collect(toSet()));
        kutsu.getOrganisaatiot().add(kutsuOrganisaatio);

        long id = kutsuService.createKutsu(kutsu);
        KutsuReadDto tallennettu = kutsuService.getKutsu(id);

        assertThat(tallennettu.getAsiointikieli()).isEqualByComparingTo(Asiointikieli.fi);
        Set<KutsuReadDto.KutsuOrganisaatioDto> organisaatiot = tallennettu.getOrganisaatiot();
        assertThat(organisaatiot).hasSize(1);
        KutsuReadDto.KutsuOrganisaatioDto tallennettuKutsuOrganisaatio = organisaatiot.iterator().next();
        Set<KutsuReadDto.KayttoOikeusRyhmaDto> kayttoOikeusRyhmat = tallennettuKutsuOrganisaatio.getKayttoOikeusRyhmat();
        assertThat(kayttoOikeusRyhmat).hasSize(1);
        KutsuReadDto.KayttoOikeusRyhmaDto tallennettuKutsuKayttoOikeusRyhma = kayttoOikeusRyhmat.iterator().next();
        assertThat(tallennettuKutsuKayttoOikeusRyhma.getNimi().getTexts()).containsExactly(new AbstractMap.SimpleEntry<>("fi", "Käyttöoikeusryhmä"));

        Kutsu entity = em.find(Kutsu.class, id);
        assertThat(entity.getSalaisuus()).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = "ROLE_APP_HENKILONHALLINTA_CRUD")
    public void deleteKutsuTest() {
        Kutsu kutsu = populate(kutsu("Matti", "Mehiläinen", "b@eaxmple.com")
                .kutsuja("1.2.4")
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                        .ryhma(kayttoOikeusRyhma("RYHMA2")))
        );
        kutsuService.deleteKutsu(kutsu.getId());
        em.flush();
        assertEquals(KutsunTila.POISTETTU, kutsu.getTila());
        assertEquals("1.2.4", kutsu.getPoistaja());
        assertNotNull(kutsu.getPoistettu());
    }
    
    @Test(expected = NotFoundException.class)
    @WithMockUser(username = "1.2.4", authorities = "ROLE_APP_HENKILONHALLINTA_CRUD")
    public void deleteKutsuOtherKutsujaFailsTest() {
        Kutsu kutsu = populate(kutsu("Matti", "Mehiläinen", "b@eaxmple.com")
                .kutsuja("1.2.5")
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                        .ryhma(kayttoOikeusRyhma("RYHMA2")))
        );
        kutsuService.deleteKutsu(kutsu.getId());
    }

    @Test
    public void getByTemporaryToken() {
        populate(KutsuPopulator.kutsu("arpa", "kuutio", "arpa@kuutio.fi").temporaryToken("123"));
        KutsuReadDto kutsu = this.kutsuService.getByTemporaryToken("123");
        assertThat(kutsu.getAsiointikieli()).isEqualTo(Asiointikieli.fi);
        assertThat(kutsu.getEtunimi()).isEqualTo("arpa");
        assertThat(kutsu.getSukunimi()).isEqualTo("kuutio");
        assertThat(kutsu.getSahkoposti()).isEqualTo("arpa@kuutio.fi");
    }

    @Test
    public void createHenkilo() {
        Kutsu kutsu = populate(KutsuPopulator.kutsu("arpa", "kuutio", "arpa@kuutio.fi")
                .temporaryToken("123")
                .hetu("hetu")
                .kutsuja("1.2.3.4.1")
                .organisaatio(KutsuOrganisaatioPopulator.kutsuOrganisaatio("1.2.0.0.1")
                        .ryhma(KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma("ryhma").withKuvaus(text("FI", "Kuvaus")))));
        Henkilo henkilo = populate(HenkiloPopulator.henkilo("1.2.3.4.5"));
        populate(HenkiloPopulator.henkilo("1.2.3.4.1"));
        doReturn("1.2.3.4.5").when(this.oppijanumerorekisteriClient).createHenkilo(anyObject());
        doReturn("1.2.3.4.5").when(this.oppijanumerorekisteriClient).getOidByHetu("hetu");
        given(this.organisaatioCacheRepository.findByOrganisaatioOid("1.2.0.0.1"))
                .willReturn(Optional.of(new OrganisaatioCache("1.2.0.0.1", "/")));
        HenkiloCreateByKutsuDto henkiloCreateByKutsuDto = new HenkiloCreateByKutsuDto("arpa",
                new KielisyysDto("fi", null), "arpauser", "stronkPassword!");

        this.kutsuService.createHenkilo("123", henkiloCreateByKutsuDto);
        assertThat(henkilo.getOidHenkilo()).isEqualTo("1.2.3.4.5");
        assertThat(henkilo.getKayttajatiedot().getUsername()).isEqualTo("arpauser");
        assertThat(henkilo.getKayttajatiedot().getPassword()).isNotEmpty();
        assertThat(henkilo.getOrganisaatioHenkilos())
                .flatExtracting(OrganisaatioHenkilo::getMyonnettyKayttoOikeusRyhmas)
                .extracting(MyonnettyKayttoOikeusRyhmaTapahtuma::getKayttoOikeusRyhma)
                .extracting(KayttoOikeusRyhma::getName)
                .containsExactly("ryhma");
        assertThat(henkilo.getOrganisaatioHenkilos())
                .flatExtracting(OrganisaatioHenkilo::getMyonnettyKayttoOikeusRyhmas)
                .extracting(MyonnettyKayttoOikeusRyhmaTapahtuma::getKasittelija)
                .extracting(Henkilo::getOidHenkilo)
                .containsExactly("1.2.3.4.1");
        assertThat(henkilo.getOrganisaatioHenkilos())
                .flatExtracting(OrganisaatioHenkilo::getOrganisaatioOid)
                .containsExactly("1.2.0.0.1");

        assertThat(kutsu.getLuotuHenkiloOid()).isEqualTo(henkilo.getOidHenkilo());
        assertThat(kutsu.getTemporaryToken()).isNull();
        assertThat(kutsu.getTila()).isEqualTo(KutsunTila.KAYTETTY);
    }

    // Existing henkilo username changes to the new one
    @Test
    public void createHenkiloHetuExistsKayttajatiedotExists() {
        Kutsu kutsu = populate(KutsuPopulator.kutsu("arpa", "kuutio", "arpa@kuutio.fi")
                .temporaryToken("123")
                .hetu("hetu")
                .kutsuja("1.2.3.4.1")
                .organisaatio(KutsuOrganisaatioPopulator.kutsuOrganisaatio("1.2.0.0.1")
                        .ryhma(KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma("ryhma").withKuvaus(text("FI", "Kuvaus")))));
        populate(HenkiloPopulator.henkilo("1.2.3.4.5"));
        Henkilo henkilo = populate(OrganisaatioHenkiloPopulator.organisaatioHenkilo(
                HenkiloPopulator.henkilo("1.2.0.0.2").withUsername("old_username"),
                "2.1.0.1"))
                .getHenkilo();
        given(this.organisaatioCacheRepository.findByOrganisaatioOid("1.2.0.0.1"))
                .willReturn(Optional.of(new OrganisaatioCache("1.2.0.0.1", "/")));
        populate(HenkiloPopulator.henkilo("1.2.3.4.1"));
        doThrow(new ExternalServiceException("",
                new CachingRestClient.HttpException(null,
                        new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 400, "reason"),
                        "message"))).when(this.oppijanumerorekisteriClient).createHenkilo(anyObject());
        doReturn("1.2.0.0.2").when(this.oppijanumerorekisteriClient).getOidByHetu("hetu");
        HenkiloCreateByKutsuDto henkiloCreateByKutsuDto = new HenkiloCreateByKutsuDto("arpa",
                new KielisyysDto("fi", null), "arpauser", "stronkPassword!");

        this.kutsuService.createHenkilo("123", henkiloCreateByKutsuDto);
        assertThat(henkilo.getOidHenkilo()).isEqualTo("1.2.0.0.2");
        assertThat(henkilo.getKayttajatiedot().getUsername()).isEqualTo("arpauser");
        assertThat(henkilo.getKayttajatiedot().getPassword()).isNotEmpty();
        assertThat(henkilo.getOrganisaatioHenkilos())
                .flatExtracting(OrganisaatioHenkilo::getMyonnettyKayttoOikeusRyhmas)
                .extracting(MyonnettyKayttoOikeusRyhmaTapahtuma::getKayttoOikeusRyhma)
                .extracting(KayttoOikeusRyhma::getName)
                .containsExactly("ryhma");
        assertThat(henkilo.getOrganisaatioHenkilos())
                .flatExtracting(OrganisaatioHenkilo::getMyonnettyKayttoOikeusRyhmas)
                .extracting(MyonnettyKayttoOikeusRyhmaTapahtuma::getKasittelija)
                .extracting(Henkilo::getOidHenkilo)
                .containsExactly("1.2.3.4.1");
        assertThat(henkilo.getOrganisaatioHenkilos())
                .flatExtracting(OrganisaatioHenkilo::getOrganisaatioOid)
                .containsExactlyInAnyOrder("1.2.0.0.1", "2.1.0.1");

        assertThat(kutsu.getLuotuHenkiloOid()).isEqualTo(henkilo.getOidHenkilo());
        assertThat(kutsu.getTemporaryToken()).isNull();
        assertThat(kutsu.getTila()).isEqualTo(KutsunTila.KAYTETTY);
    }

    @Test
    public void createHenkiloWithHakaIdentifier() {
        Kutsu kutsu = populate(KutsuPopulator.kutsu("arpa", "kuutio", "arpa@kuutio.fi")
                .hakaIdentifier("!haka%Identifier1/")
                .temporaryToken("123")
                .hetu("hetu")
                .kutsuja("1.2.3.4.1")
                .organisaatio(KutsuOrganisaatioPopulator.kutsuOrganisaatio("1.2.0.0.1")
                        .ryhma(KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma("ryhma").withKuvaus(text("FI", "Kuvaus")))));
        Henkilo henkilo = populate(HenkiloPopulator.henkilo("1.2.3.4.5"));
        populate(HenkiloPopulator.henkilo("1.2.3.4.1"));
        doReturn("1.2.3.4.5").when(this.oppijanumerorekisteriClient).createHenkilo(anyObject());
        doReturn("1.2.3.4.5").when(this.oppijanumerorekisteriClient).getOidByHetu("hetu");
        given(this.organisaatioCacheRepository.findByOrganisaatioOid("1.2.0.0.1"))
                .willReturn(Optional.of(new OrganisaatioCache("1.2.0.0.1", "/")));
        HenkiloCreateByKutsuDto henkiloCreateByKutsuDto = new HenkiloCreateByKutsuDto("arpa",
                new KielisyysDto("fi", null), null, null);

        this.kutsuService.createHenkilo("123", henkiloCreateByKutsuDto);
        assertThat(henkilo.getOidHenkilo()).isEqualTo("1.2.3.4.5");
        assertThat(henkilo.getKayttajatiedot().getUsername()).matches("hakaIdentifier1[\\d]{3,3}");
        assertThat(henkilo.getKayttajatiedot().getPassword()).isNull();
        assertThat(henkilo.getOrganisaatioHenkilos())
                .flatExtracting(OrganisaatioHenkilo::getMyonnettyKayttoOikeusRyhmas)
                .extracting(MyonnettyKayttoOikeusRyhmaTapahtuma::getKayttoOikeusRyhma)
                .extracting(KayttoOikeusRyhma::getName)
                .containsExactly("ryhma");
        assertThat(henkilo.getOrganisaatioHenkilos())
                .flatExtracting(OrganisaatioHenkilo::getMyonnettyKayttoOikeusRyhmas)
                .extracting(MyonnettyKayttoOikeusRyhmaTapahtuma::getKasittelija)
                .extracting(Henkilo::getOidHenkilo)
                .containsExactly("1.2.3.4.1");
        assertThat(henkilo.getOrganisaatioHenkilos())
                .flatExtracting(OrganisaatioHenkilo::getOrganisaatioOid)
                .containsExactly("1.2.0.0.1");

        assertThat(henkilo.getIdentifications())
                .filteredOn(identification -> identification.getIdpEntityId().equals(HAKA_AUTHENTICATION_IDP))
                .extracting(Identification::getIdentifier)
                .containsExactlyInAnyOrder("!haka%Identifier1/");

        assertThat(kutsu.getLuotuHenkiloOid()).isEqualTo(henkilo.getOidHenkilo());
        assertThat(kutsu.getTemporaryToken()).isNull();
        assertThat(kutsu.getTila()).isEqualTo(KutsunTila.KAYTETTY);
    }

    // Another haka identifier will be added to an existing user credentials
    @Test
    public void createHenkiloWithHakaIdentifierHetuExists() {
        Kutsu kutsu = populate(KutsuPopulator.kutsu("arpa", "kuutio", "arpa@kuutio.fi")
                .hakaIdentifier("!haka%Identifier1/")
                .temporaryToken("123")
                .hetu("hetu")
                .kutsuja("1.2.3.4.1")
                .organisaatio(KutsuOrganisaatioPopulator.kutsuOrganisaatio("1.2.0.0.1")
                        .ryhma(KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma("ryhma").withKuvaus(text("FI", "Kuvaus")))));
        Henkilo henkilo = populate(IdentificationPopulator.identification(HAKA_AUTHENTICATION_IDP,
                "old_identifier",
                HenkiloPopulator.henkilo("1.2.3.4.5")))
                .getHenkilo();
        populate(HenkiloPopulator.henkilo("1.2.3.4.1"));
        doReturn("1.2.3.4.5").when(this.oppijanumerorekisteriClient).createHenkilo(anyObject());
        doReturn("1.2.3.4.5").when(this.oppijanumerorekisteriClient).getOidByHetu("hetu");
        given(this.organisaatioCacheRepository.findByOrganisaatioOid("1.2.0.0.1"))
                .willReturn(Optional.of(new OrganisaatioCache("1.2.0.0.1", "/")));
        HenkiloCreateByKutsuDto henkiloCreateByKutsuDto = new HenkiloCreateByKutsuDto("arpa",
                new KielisyysDto("fi", null), null, null);

        this.kutsuService.createHenkilo("123", henkiloCreateByKutsuDto);
        assertThat(henkilo.getOidHenkilo()).isEqualTo("1.2.3.4.5");
        assertThat(henkilo.getKayttajatiedot().getUsername()).matches("hakaIdentifier1[\\d]{3,3}");
        assertThat(henkilo.getKayttajatiedot().getPassword()).isNull();
        assertThat(henkilo.getOrganisaatioHenkilos())
                .flatExtracting(OrganisaatioHenkilo::getMyonnettyKayttoOikeusRyhmas)
                .extracting(MyonnettyKayttoOikeusRyhmaTapahtuma::getKayttoOikeusRyhma)
                .extracting(KayttoOikeusRyhma::getName)
                .containsExactly("ryhma");
        assertThat(henkilo.getOrganisaatioHenkilos())
                .flatExtracting(OrganisaatioHenkilo::getMyonnettyKayttoOikeusRyhmas)
                .extracting(MyonnettyKayttoOikeusRyhmaTapahtuma::getKasittelija)
                .extracting(Henkilo::getOidHenkilo)
                .containsExactly("1.2.3.4.1");
        assertThat(henkilo.getOrganisaatioHenkilos())
                .flatExtracting(OrganisaatioHenkilo::getOrganisaatioOid)
                .containsExactly("1.2.0.0.1");

        assertThat(henkilo.getIdentifications())
                .filteredOn(identification -> identification.getIdpEntityId().equals(HAKA_AUTHENTICATION_IDP))
                .extracting(Identification::getIdentifier)
                .containsExactlyInAnyOrder("!haka%Identifier1/", "old_identifier");

        assertThat(kutsu.getLuotuHenkiloOid()).isEqualTo(henkilo.getOidHenkilo());
        assertThat(kutsu.getTemporaryToken()).isNull();
        assertThat(kutsu.getTila()).isEqualTo(KutsunTila.KAYTETTY);
    }
}
