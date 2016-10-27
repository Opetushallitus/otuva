package fi.vm.sade.kayttooikeus.service.it;

import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioViiteRepository;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.dto.PalveluRoooliDto;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.util.AccessRightManagementUtils;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusPopulator.oikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.viite;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloKayttoOikeusPopulator.myonnettyKayttoOikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.PalveluPopulator.palvelu;
import static fi.vm.sade.kayttooikeus.repositories.populate.TextGroupPopulator.text;
import static fi.vm.sade.kayttooikeus.util.JsonUtil.readJson;
import static java.util.Collections.singletonList;
import static org.joda.time.LocalDate.now;
import static org.joda.time.Period.months;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.matches;

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
        MyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma1 = populate(myonnettyKayttoOikeus(
                    organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                    kayttoOikeusRyhma("RYHMA")
                            .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                            .withOikeus(oikeus("KOODISTO", "READ"))
                )),
            tapahtuma2 = populate(myonnettyKayttoOikeus(
                    organisaatioHenkilo(henkilo("1.2.3.4.5"), "4.5.6.7.8"),
                    kayttoOikeusRyhma("RYHMA2").withKuvaus(text("FI", "Koodistonhallinta"))
                            .withOikeus(oikeus("KOODISTO", "CRUD"))
            ));
        
        List<KayttoOikeusHistoriaDto> list = kayttoOikeusService.listMyonnettyKayttoOikeusHistoriaForCurrentUser();
        assertEquals(3, list.size());
        assertEquals(tapahtuma2.getAikaleima(), list.get(0).getAikaleima());
        assertEquals(tapahtuma2.getKasittelija().getOidHenkilo(), list.get(0).getKasittelija());
        assertEquals(tapahtuma2.getOrganisaatioHenkilo().getOrganisaatioOid(), list.get(0).getOrganisaatioOid());
        assertEquals(tapahtuma2.getOrganisaatioHenkilo().getTehtavanimike(), list.get(0).getTehtavanimike());
        assertEquals(KayttoOikeudenTila.MYONNETTY, list.get(0).getTila());
        assertEquals(KayttoOikeusTyyppi.KOOSTEROOLI, list.get(0).getTyyppi());
        assertEquals(tapahtuma2.getVoimassaAlkuPvm(), list.get(0).getVoimassaAlkuPvm());
        assertEquals(tapahtuma2.getVoimassaLoppuPvm(), list.get(0).getVoimassaLoppuPvm());
        assertEquals(tapahtuma2.getKayttoOikeusRyhma().getKayttoOikeus().iterator().next().getId().longValue(),
                list.get(0).getKayttoOikeusId());
        assertEquals("Koodistonhallinta", list.get(0).getKuvaus().get("FI"));
    }

    @Test
    public void findToBeExpiringMyonnettyKayttoOikeusTest() {
        MyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA").withKuvaus(text("FI", "kuvaus"))
                        .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                        .withOikeus(oikeus("KOODISTO", "READ"))
            ).voimassaPaattyen(now().plusMonths(1)));
        List<ExpiringKayttoOikeusDto> oikeus = kayttoOikeusService.findToBeExpiringMyonnettyKayttoOikeus(now(), months(1));
        assertEquals(1, oikeus.size());
        assertEquals(tapahtuma.getId(), oikeus.get(0).getMyonnettyTapahtumaId());
        assertEquals("1.2.3.4.5", oikeus.get(0).getHenkiloOid());
        assertEquals(now().plusMonths(1), oikeus.get(0).getVoimassaLoppuPvm());
        assertEquals("kuvaus", oikeus.get(0).getRyhmaDescription().get("FI"));
        assertEquals("RYHMA", oikeus.get(0).getRyhmaName());
    }

    @Test
    @WithMockUser(username = "1.2.3.4.6")
    public void listPossibleRyhmasByOrganizationTest(){
        OrganisaatioPerustieto organisaatioPerustieto = new OrganisaatioPerustieto();
        organisaatioPerustieto.setAliOrganisaatioMaara(3);
        given(this.organisaatioClient.listActiveOganisaatioPerustiedot(anyListOf(String.class)))
                .willReturn(singletonList(organisaatioPerustieto));

        given(this.organisaatioViiteRepository.findByKayttoOikeusRyhmaId(any()))
                .willReturn(singletonList(OrganisaatioViite.builder()
                        .organisaatioTyyppi("123.123.123")
                        .build()));

        MyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.6"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA").withKuvaus(text("FI", "kuvaus"))
                        .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                        .withOikeus(oikeus("KOODISTO", "READ"))
        ).voimassaPaattyen(now().plusMonths(1)));

        List<KayttoOikeusRyhmaDto> ryhmas = kayttoOikeusService.listPossibleRyhmasByOrganization("123.123.123");
        assertEquals(1, ryhmas.size());
        assertEquals("RYHMA", ryhmas.get(0).getName());
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
        KayttoOikeus oikeus = populate(oikeus("HENKILOHALLINTA", "CRUD"));

        Palvelu palvelu1 = populate(palvelu("HENKILOPALVELU").kuvaus(text("FI", "Henkilöpalvelu")
                .put("EN", "Person service"))),
                palvelu2 = populate(palvelu("KOODISTO"));
        palvelu2.getKayttoOikeus().add(oikeus);

        populate(kayttoOikeusRyhma("RYHMA1").withKuvaus(text("FI", "Käyttäjähallinta")
                .put("EN", "User management"))
                .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                .withOikeus(oikeus("KOODISTO", "READ")));
        Long id = populate(kayttoOikeusRyhma("RYHMA2").withKuvaus(text("FI", "Koodistonhallinta")
                .put("EN", "Code management"))
                .withViite(viite(kayttoOikeusRyhma("RYHMA1"), "TYYPPI"))
                .withOikeus(oikeus("KOODISTO", "CRUD"))).getId();

        List<PalveluRoooliDto> roolis = kayttoOikeusService.findPalveluRoolisByKayttoOikeusRyhma(id);
        assertEquals(1L, roolis.size());
        assertEquals("KOODISTO", roolis.get(0).getPalveluName());
        assertEquals("CRUD", roolis.get(0).getRooli());
    }
}
