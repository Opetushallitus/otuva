package fi.vm.sade.kayttooikeus.service.it;

import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioViiteRepository;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator;
import fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusPopulator.oikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaMyontoViitePopulator.kayttoOikeusRyhmaMyontoViite;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.viite;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloKayttoOikeusPopulator.myonnettyKayttoOikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.PalveluPopulator.palvelu;
import static fi.vm.sade.kayttooikeus.repositories.populate.TextGroupPopulator.text;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

@RunWith(SpringRunner.class)
public class KayttoOikeusServiceTest extends AbstractServiceIntegrationTest {
    @Autowired
    private KayttoOikeusService kayttoOikeusService;

    @MockBean
    private OrganisaatioViiteRepository organisaatioViiteRepository;

    @MockBean
    private OrganisaatioClient organisaatioClient;

    @Test
    public void listAllKayttoOikeusRyhmasTest() {
        populate(kayttoOikeusRyhma("RYHMA1").withKuvaus(text("FI", "Käyttäjähallinta")
                                .put("EN", "User management"))
                .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                .withOikeus(oikeus("KOODISTO", "READ")));
        populate(kayttoOikeusRyhma("RYHMA2").withKuvaus(text("FI", "Koodistonhallinta")
                        .put("EN", "Code management"))
                .withViite(viite(kayttoOikeusRyhma("RYHMA1"), "TYYPPI"))
                .withOikeus(oikeus("KOODISTO", "CRUD")));
        
        List<KayttoOikeusRyhmaDto> ryhmas = kayttoOikeusService.listAllKayttoOikeusRyhmas();
        assertEquals(2, ryhmas.size());

    }

    @Test
    public void listKayttoOikeusByPalveluTest() {
        populate(oikeus("HENKILOHALLINTA", "CRUD").kuvaus(text("FI", "Käsittelyoikeus")
                                                        .put("EN", "Admin")));
        populate(oikeus("HENKILOHALLINTA", "READ").kuvaus(text("FI", "Lukuoikeus")));
        populate(oikeus("KOODISTO", "CRUD"));

        List<PalveluKayttoOikeusDto> results = kayttoOikeusService.listKayttoOikeusByPalvelu("HENKILOHALLINTA");
        assertEquals(2, results.size());
        assertEquals("CRUD", results.get(0).getRooli());
        assertEquals("Käsittelyoikeus", results.get(0).getOikeusLangs().get("FI"));
        assertEquals("Admin", results.get(0).getOikeusLangs().get("EN"));
        assertEquals("Lukuoikeus", results.get(1).getOikeusLangs().get("FI"));
    }
    
    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void listMyonnettyKayttoOikeusHistoriaForCurrentUser() {
        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA")
                        .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                        .withOikeus(oikeus(palvelu("KOODISTO").kuvaus(text("FI", "Palvelukuvaus")), "READ"))
        ));
        MyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "4.5.6.7.8"),
                kayttoOikeusRyhma("RYHMA2").withKuvaus(text("FI", "Koodistonhallinta"))
                        .withOikeus(oikeus("KOODISTO", "CRUD")
                                .kuvaus(text("FI", "Kirjoitusoikeus")))
        ));
        
        List<KayttoOikeusHistoriaDto> list = kayttoOikeusService.listMyonnettyKayttoOikeusHistoriaForCurrentUser();
        assertEquals(3, list.size());
        KayttoOikeusHistoriaDto kayttoOikeusHistoriaResult = list.stream().filter(kayttoOikeusHistoriaDto -> kayttoOikeusHistoriaDto.getOrganisaatioOid().equals(tapahtuma.getOrganisaatioHenkilo().getOrganisaatioOid())).findFirst().orElseThrow(RuntimeException::new);
        assertEquals(tapahtuma.getAikaleima(), kayttoOikeusHistoriaResult.getAikaleima());
        assertEquals(tapahtuma.getKasittelija().getOidHenkilo(), kayttoOikeusHistoriaResult.getKasittelija());
        assertEquals(tapahtuma.getOrganisaatioHenkilo().getOrganisaatioOid(), kayttoOikeusHistoriaResult.getOrganisaatioOid());
        assertEquals(tapahtuma.getOrganisaatioHenkilo().getTehtavanimike(), kayttoOikeusHistoriaResult.getTehtavanimike());
        assertEquals(KayttoOikeudenTila.MYONNETTY, kayttoOikeusHistoriaResult.getTila());
        assertEquals(KayttoOikeusTyyppi.KOOSTEROOLI, kayttoOikeusHistoriaResult.getTyyppi());
        assertEquals(tapahtuma.getVoimassaAlkuPvm(), kayttoOikeusHistoriaResult.getVoimassaAlkuPvm());
        assertEquals(tapahtuma.getVoimassaLoppuPvm(), kayttoOikeusHistoriaResult.getVoimassaLoppuPvm());
        assertEquals(tapahtuma.getKayttoOikeusRyhma().getKayttoOikeus().iterator().next().getId().longValue(),
                kayttoOikeusHistoriaResult.getKayttoOikeusId());
        assertEquals("CRUD", kayttoOikeusHistoriaResult.getRooli());
        assertEquals("KOODISTO", kayttoOikeusHistoriaResult.getPalvelu());
        assertEquals("Koodistonhallinta", kayttoOikeusHistoriaResult.getKuvaus().get("FI"));
        assertEquals("Kirjoitusoikeus", kayttoOikeusHistoriaResult.getKayttoOikeusKuvaus().get("FI"));
        assertEquals("Palvelukuvaus", kayttoOikeusHistoriaResult.getPalveluKuvaus().get("FI"));
    }

    @Test
    public void findToBeExpiringMyonnettyKayttoOikeusTest() {
        MyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA").withKuvaus(text("FI", "kuvaus"))
                        .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                        .withOikeus(oikeus("KOODISTO", "READ"))
            ).voimassaPaattyen(LocalDate.now().plusMonths(1)));
        List<ExpiringKayttoOikeusDto> oikeus = kayttoOikeusService.findToBeExpiringMyonnettyKayttoOikeus(LocalDate.now(), Period.ofMonths(1));
        assertEquals(1, oikeus.size());
        assertEquals(tapahtuma.getId(), oikeus.get(0).getMyonnettyTapahtumaId());
        assertEquals("1.2.3.4.5", oikeus.get(0).getHenkiloOid());
        assertEquals(LocalDate.now().plusMonths(1), oikeus.get(0).getVoimassaLoppuPvm());
        assertEquals("kuvaus", oikeus.get(0).getRyhmaDescription().get("FI"));
        assertEquals("RYHMA", oikeus.get(0).getRyhmaName());
    }

    @Test
    @WithMockUser(username = "1.2.3.4.6")
    public void listPossibleRyhmasByOrganizationTest(){
        OrganisaatioPerustieto organisaatioPerustieto = new OrganisaatioPerustieto();
        given(this.organisaatioClient.listActiveOganisaatioPerustiedotRecursiveCached(any()))
                .willReturn(singletonList(organisaatioPerustieto));

        Long ryhmaId = populate(kayttoOikeusRyhma("RYHMA").withKuvaus(text("FI", "Käyttäjähallinta")
                .put("EN", "User management"))
                .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                .withOikeus(oikeus("KOODISTO", "READ"))
                .withViite(viite(kayttoOikeusRyhma("test"), "organisaatio tyyppi")))
                .getId();

        given(this.organisaatioViiteRepository.findByKayttoOikeusRyhmaIds(any()))
                .willReturn(singletonList(OrganisaatioViiteDto.builder()
                        .organisaatioTyyppi("123.123.123")
                        .id(1L).kayttoOikeusRyhmaId(ryhmaId).build()));

        List<KayttoOikeusRyhmaDto> ryhmas = kayttoOikeusService.listPossibleRyhmasByOrganization("123.123.123");
        assertEquals(1, ryhmas.size());
        assertEquals("RYHMA", ryhmas.get(0).getName());
        assertEquals(1, ryhmas.get(0).getOrganisaatioViite().size());
        assertEquals("123.123.123", ryhmas.get(0).getOrganisaatioViite().get(0).getOrganisaatioTyyppi());
        assertEquals("Käyttäjähallinta", ryhmas.get(0).getDescription().get("FI"));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void findKayttoOikeusRyhmaTest(){
        populate(kayttoOikeusRyhma("RYHMA1").withKuvaus(text("FI", "Käyttäjähallinta")
                .put("EN", "User management"))
                .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                .withOikeus(oikeus("KOODISTO", "READ")));
        Long id = populate(kayttoOikeusRyhma("RYHMA2").withKuvaus(text("FI", "Koodistonhallinta")
                .put("EN", "Code management"))
                .withViite(viite(kayttoOikeusRyhma("RYHMA1"), "TYYPPI"))
                .withOikeus(oikeus("KOODISTO", "CRUD"))).getId();

        KayttoOikeusRyhmaDto ryhma = kayttoOikeusService.findKayttoOikeusRyhma(id);
        assertNotNull(ryhma);
        assertEquals("RYHMA2", ryhma.getName());
    }


    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void findPalveluRoolisByKayttoOikeusRyhmaTest(){
        populate(palvelu("HENKILOPALVELU").kuvaus(text("FI", "Henkilöpalvelu").put("EN", "Person service")));
        Palvelu palvelu = populate(palvelu("KOODISTO").kuvaus(text("FI", "palvelun kuvaus")
                        .put("EN", "kuv en")
                        .put("SV", "kuvaus på sv")));
        palvelu.getKayttoOikeus().add(populate(oikeus("HENKILOHALLINTA", "CRUD")));

        populate(kayttoOikeusRyhma("RYHMA1").withKuvaus(text("FI", "Käyttäjähallinta")
                .put("EN", "User management"))
                .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                .withOikeus(oikeus("KOODISTO", "READ")));
        Long id = populate(kayttoOikeusRyhma("RYHMA2").withKuvaus(text("FI", "Koodistonhallinta")
                .put("EN", "Code management"))
                .withViite(viite(kayttoOikeusRyhma("RYHMA1"), "TYYPPI"))
                .withOikeus(oikeus("KOODISTO", "CRUD"))).getId();

        List<PalveluRooliDto> roolis = kayttoOikeusService.findPalveluRoolisByKayttoOikeusRyhma(id);
        assertEquals(1L, roolis.size());
        assertEquals("KOODISTO", roolis.get(0).getPalveluName());
        assertEquals("CRUD", roolis.get(0).getRooli());
        assertEquals(3, roolis.get(0).getPalveluTexts().getTexts().size());
        assertEquals("palvelun kuvaus", roolis.get(0).getPalveluTexts().get("FI"));
        assertEquals("kuv en", roolis.get(0).getPalveluTexts().get("EN"));
        assertEquals("kuvaus på sv", roolis.get(0).getPalveluTexts().get("SV"));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void findHenkilotByKayttoOikeusRyhma(){

        KayttoOikeusRyhmaPopulator pop = kayttoOikeusRyhma("RYHMA1").withKuvaus(text("FI", "Koodistonhallinta")
                .put("EN", "Code management"))
                .withOikeus(oikeus("KOODISTO", "CRUD"));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"), pop).voimassaPaattyen(LocalDate.now().plusMonths(3)).voimassaAlkaen(LocalDate.now().minusMonths(1)));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("5.6.7.8.9"), "3.4.5.6.7"), pop).voimassaPaattyen(LocalDate.now().plusMonths(2)).voimassaAlkaen(LocalDate.now().minusMonths(1)));

        KayttoOikeusRyhmaPopulator pop2 = kayttoOikeusRyhma("RYHMA2").withKuvaus(text("FI", "testiryhma")
                .put("EN", "testgroup"))
                .withOikeus(oikeus("HAKUAPP", "CRUD"));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"), pop2).voimassaPaattyen(LocalDate.now().minusMonths(1)).voimassaAlkaen(LocalDate.now().minusMonths(2)));

        RyhmanHenkilotDto henkilot = kayttoOikeusService.findHenkilotByKayttoOikeusRyhma(populate(pop).getId());
        assertEquals(2, henkilot.getPersonOids().size());

        RyhmanHenkilotDto henkilot2 = kayttoOikeusService.findHenkilotByKayttoOikeusRyhma(populate(pop2).getId());
        assertEquals(0, henkilot2.getPersonOids().size());

    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void findSubRyhmasByMasterRyhmaTest(){
        Long id = populate(kayttoOikeusRyhma("RYHMA").withKuvaus(text("FI", "Käyttäjähallinta")
                .put("EN", "User management"))
                .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                .withOikeus(oikeus("KOODISTO", "READ"))).getId();
        populate(kayttoOikeusRyhmaMyontoViite(23432L, id));

        List<KayttoOikeusRyhmaDto> ryhmas = kayttoOikeusService.findSubRyhmasByMasterRyhma(23432L);
        assertEquals(1, ryhmas.size());
        assertEquals("RYHMA", ryhmas.get(0).getName());

        ryhmas = kayttoOikeusService.findSubRyhmasByMasterRyhma(111L);
        assertEquals(0, ryhmas.size());
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void createKayttoOikeusRyhmaAndUpdateTest() {
        KayttoOikeus oikeus = populate(oikeus("HENKILOHALLINTA", "CRUD"));
        Palvelu palvelu = populate(palvelu("JOKUPALVELU").kuvaus(text("FI", "joku palvelu")
                .put("EN", "Person service")));
        palvelu.getKayttoOikeus().add(oikeus);

        KayttoOikeusRyhmaModifyDto ryhma = KayttoOikeusRyhmaModifyDto.builder()
                .nimi(new TextGroupDto()
                        .put("FI", "ryhmäname")
                        .put("SV", "ryhmäname sv")
                        .put("EN", "ryhmäname en"))
                .kuvaus(new TextGroupDto()
                        .put("FI", "ryhmäkuvaus")
                        .put("SV", "ryhmäkuvaus sv")
                        .put("EN", "ryhmäkuvaus en"))
                .rooliRajoite("roolirajoite")
                .palvelutRoolit(singletonList(PalveluRooliModifyDto.builder()
                        .rooli("CRUD")
                        .palveluName("HENKILOHALLINTA")
                        .build()))
                .organisaatioTyypit(singletonList("org tyyppi"))
                .passivoitu(false)
                .ryhmaRestriction(false)
                .build();

        long createdRyhmaId = kayttoOikeusService.createKayttoOikeusRyhma(ryhma);
        KayttoOikeusRyhmaDto createdRyhma = kayttoOikeusService.findKayttoOikeusRyhma(createdRyhmaId);

        assertNotNull(createdRyhma);
        assertTrue(createdRyhma.getName().startsWith("ryhmäname_"));
        assertTrue(createdRyhma.getDescription().get("FI").contentEquals("ryhmäname"));
        assertTrue(createdRyhma.getKuvaus().get("FI").contentEquals("ryhmäkuvaus"));
        assertEquals(1, createdRyhma.getOrganisaatioViite().size());
        assertEquals("org tyyppi", createdRyhma.getOrganisaatioViite().get(0).getOrganisaatioTyyppi());
        assertFalse(createdRyhma.isPassivoitu());
        assertFalse(createdRyhma.isRyhmaRestriction());

        ryhma.setNimi(new TextGroupDto().put("FI", "uusi nimi"));
        ryhma.setKuvaus(new TextGroupDto().put("FI", "uusi kuvaus"));
        ryhma.setRooliRajoite("uusi rajoite");
        ryhma.setOrganisaatioTyypit(singletonList("uusi org tyyppi"));
        kayttoOikeusService.updateKayttoOikeusForKayttoOikeusRyhma(createdRyhmaId, ryhma);

        createdRyhma = kayttoOikeusService.findKayttoOikeusRyhma(createdRyhmaId);
        assertTrue(createdRyhma.getDescription().get("FI").contentEquals("uusi nimi"));
        assertTrue(createdRyhma.getKuvaus().get("FI").contentEquals("uusi kuvaus"));
        assertTrue(createdRyhma.getOrganisaatioViite().get(0).getOrganisaatioTyyppi().contentEquals("uusi org tyyppi"));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void createKayttoOikeusTest(){
        KayttoOikeus oikeus = populate(oikeus("HENKILOHALLINTA", "CRUD"));
        Palvelu palvelu = populate(palvelu("JOKUPALVELU").kuvaus(text("FI", "joku palvelu")
                .put("EN", "Person service").put("SV", "palvelu sv")));
        palvelu.getKayttoOikeus().add(oikeus);

        KayttoOikeusCreateDto ko = KayttoOikeusCreateDto.builder()
                .rooli("rooli")
                .palveluName("HENKILOHALLINTA")
                .textGroup(new TextGroupDto().put("FI", "kuvaus"))
                .build();
        long id = kayttoOikeusService.createKayttoOikeus(ko);
        KayttoOikeusDto dto = kayttoOikeusService.findKayttoOikeusById(id);
        assertEquals("rooli", dto.getRooli());
        assertEquals("HENKILOHALLINTA", dto.getPalvelu().getName());
        assertEquals("kuvaus", dto.getTextGroup().get("FI"));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos(){
        MyonnettyKayttoOikeusRyhmaTapahtuma mko = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA2").withKuvaus(text("FI", "Koodistonhallinta")
                        .put("EN", "Code management"))
                        .withViite(viite(kayttoOikeusRyhma("RYHMA1"), "TYYPPI"))
                        .withOikeus(oikeus("KOODISTO", "CRUD"))
        ));


        given(this.organisaatioViiteRepository.findByKayttoOikeusRyhmaIds(any()))
                .willReturn(asList(OrganisaatioViiteDto.builder()
                                .organisaatioTyyppi("123.123.123")
                                .id(1L)
                                .kayttoOikeusRyhmaId(mko.getKayttoOikeusRyhma().getId())
                                .build(),
                        OrganisaatioViiteDto.builder()
                                .organisaatioTyyppi("3.4.5.6.7")
                                .id(2L)
                                .kayttoOikeusRyhmaId(mko.getKayttoOikeusRyhma().getId())
                                .build()));

        List<MyonnettyKayttoOikeusDto> list = kayttoOikeusService.listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos("1.2.3.4.5", "3.4.5.6.7", "1.2.3.4.5");
        assertEquals(1, list.size());

        list = kayttoOikeusService.listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos("1.2.3.4.5", "3.4.5.6.madeup", "1.2.3.4.5");
        assertEquals(0, list.size());
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void listMyonnettyKayttoOikeusRyhmasMergedWithHenkilosWithMyontoViite(){
        MyonnettyKayttoOikeusRyhmaTapahtuma mko = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA2").withKuvaus(text("FI", "Koodistonhallinta")
                        .put("EN", "Code management"))
                        .withViite(viite(kayttoOikeusRyhma("RYHMA1"), "TYYPPI"))
                        .withOikeus(oikeus("KOODISTO", "CRUD"))
        ));

        MyonnettyKayttoOikeusRyhmaTapahtuma mko2 = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA3").withKuvaus(text("FI", "Koodistonhallinta")
                        .put("EN", "Code management"))
                        .withViite(viite(kayttoOikeusRyhma("RYHMA1"), "TYYPPI"))
                        .withOikeus(oikeus("KOODISTO", "CRUD"))
        ));

        populate(kayttoOikeusRyhmaMyontoViite(mko.getKayttoOikeusRyhma().getId(), mko2.getId()));
        Long viiteKoId = mko.getKayttoOikeusRyhma().getOrganisaatioViite().iterator().next().getKayttoOikeusRyhma().getId();

        given(this.organisaatioViiteRepository.findByKayttoOikeusRyhmaIds(any()))
                .willReturn(singletonList(OrganisaatioViiteDto.builder()
                                .organisaatioTyyppi("3.4.5.6.7")
                                .id(123123L)
                                .kayttoOikeusRyhmaId(viiteKoId)
                                .build()));


        List<MyonnettyKayttoOikeusDto> list = kayttoOikeusService.listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos("1.2.3.4.5", "3.4.5.6.7", "1.2.3.4.5");
        assertEquals(1, list.size());
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void findKayttoOikeusRyhmasByKayttoOikeusIdsTest() {
        KayttoOikeusRyhma ryhma = populate(kayttoOikeusRyhma("RYHMA")
                .withKuvaus(text("FI", "Käyttäjähallinta")
                        .put("EN", "User management")
                        .put("SV", "på svenska"))
                .withViite(viite(kayttoOikeusRyhma("RYHMA1"), "TYYPPI"))
                .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                .withOikeus(oikeus("KOODISTO", "READ")));
        ryhma.setRooliRajoite("roolirajoite");
        Long oikeusId = ryhma.getKayttoOikeus().iterator().next().getId();

        given(this.organisaatioViiteRepository.findByKayttoOikeusRyhmaIds(any()))
                .willReturn(singletonList(OrganisaatioViiteDto.builder()
                        .organisaatioTyyppi("123.123.123")
                        .id(1L)
                        .kayttoOikeusRyhmaId(ryhma.getId())
                        .build()));

        List<KayttoOikeusRyhmaDto> ryhmas = kayttoOikeusService.findKayttoOikeusRyhmasByKayttoOikeusList(singletonMap("HENKILOHALLINTA", "CRUD"));
        assertEquals(1, ryhmas.size());
        assertEquals("RYHMA", ryhmas.get(0).getName());
        assertEquals("roolirajoite", ryhmas.get(0).getRooliRajoite());
        assertEquals("Käyttäjähallinta", ryhmas.get(0).getDescription().get("FI"));
        assertEquals("User management", ryhmas.get(0).getDescription().get("EN"));
        assertEquals("på svenska", ryhmas.get(0).getDescription().get("SV"));
        assertEquals("123.123.123", ryhmas.get(0).getOrganisaatioViite().get(0).getOrganisaatioTyyppi());

        given(this.organisaatioViiteRepository.findByKayttoOikeusRyhmaIds(any())).willReturn(emptyList());
        ryhmas = kayttoOikeusService.findKayttoOikeusRyhmasByKayttoOikeusList(emptyMap());
        assertEquals(0, ryhmas.size());
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void findAuthorizationDataByOidTest() throws Exception {
        OrganisaatioHenkiloPopulator orgHenkilo = organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7");
        populate(myonnettyKayttoOikeus(orgHenkilo, kayttoOikeusRyhma("RYHMA2")
                .withOikeus(oikeus("KOODISTO", "READ"))
                .withOikeus(oikeus("KOODISTO", "CRUD"))));

        OrganisaatioHenkilo organisaatioHenkilo = populate(myonnettyKayttoOikeus(orgHenkilo, kayttoOikeusRyhma("RYHMA3")
                .withOikeus(oikeus("KOODISTO", "CRUD"))
                .withOikeus(oikeus("HENKILOHALLINTA", "CRUD")))).getOrganisaatioHenkilo();

        populate(myonnettyKayttoOikeus(orgHenkilo, kayttoOikeusRyhma("RYHMA4")
                .withOikeus(oikeus("HENKILOHALLINTA", "READ2")))
                .voimassaAlkaen(LocalDate.now().plusDays(1))
                .voimassaPaattyen(LocalDate.now().plusDays(2)));

        populate(myonnettyKayttoOikeus(orgHenkilo, kayttoOikeusRyhma("RYHMA4")
                .withOikeus(oikeus("HENKILOHALLINTA", "READ3")))
                .voimassaAlkaen(LocalDate.now().minusDays(2))
                .voimassaPaattyen(LocalDate.now().minusDays(1)));

        AuthorizationDataDto authData = kayttoOikeusService.findAuthorizationDataByOid("1.2.3.4.5");
        List<AccessRightTypeDto> accessRights = authData.getAccessrights().getAccessRight();
        assertEquals(3, accessRights.size());
        List<AccessRightTypeDto> koodistoList = accessRights.stream()
                .filter(dto -> dto.getPalvelu().equals("KOODISTO"))
                .collect(Collectors.toList());
        assertEquals(2, koodistoList.size());
        assertTrue(koodistoList.stream().allMatch(dto -> Arrays.asList("CRUD", "READ").contains(dto.getRooli())));

        List<AccessRightTypeDto> henkilohallinta = accessRights.stream()
                .filter(dto -> dto.getPalvelu().equals("HENKILOHALLINTA"))
                .collect(Collectors.toList());
        assertEquals(1, henkilohallinta.size());
        assertEquals("CRUD", henkilohallinta.get(0).getRooli());

        List<GroupTypeDto> group = authData.getGroups().getGroup();
        assertEquals(2, group.size());
        assertTrue(group.stream().allMatch(dto -> Arrays.asList("RYHMA2", "RYHMA3").contains(dto.getNimi())));

        organisaatioHenkilo.setPassivoitu(true);
        authData = kayttoOikeusService.findAuthorizationDataByOid("1.2.3.4.5");
        assertEquals(0, authData.getAccessrights().getAccessRight().size());
        assertEquals(0, authData.getGroups().getGroup().size());

        organisaatioHenkilo.setPassivoitu(false);
        authData = kayttoOikeusService.findAuthorizationDataByOid("1.2.3.4.5");
        assertEquals(3, authData.getAccessrights().getAccessRight().size());
        assertEquals(2, authData.getGroups().getGroup().size());

        authData = kayttoOikeusService.findAuthorizationDataByOid("1.1.1.madeup.1");
        assertEquals(0, authData.getAccessrights().getAccessRight().size());
        assertEquals(0, authData.getGroups().getGroup().size());
    }
}
