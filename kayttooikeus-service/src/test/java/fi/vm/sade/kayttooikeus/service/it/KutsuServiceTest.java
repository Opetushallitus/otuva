package fi.vm.sade.kayttooikeus.service.it;

import com.google.common.collect.Sets;
import fi.vm.sade.kayttooikeus.aspects.HenkiloHelper;
import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.dto.enumeration.KutsuView;
import fi.vm.sade.kayttooikeus.enumeration.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.criteria.KutsuCriteria;
import fi.vm.sade.kayttooikeus.repositories.populate.*;
import fi.vm.sade.kayttooikeus.service.EmailService;
import fi.vm.sade.kayttooikeus.service.KayttooikeusAnomusService;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import fi.vm.sade.kayttooikeus.service.OrganisaatioService;
import fi.vm.sade.kayttooikeus.service.exception.ForbiddenException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Stream;

import static fi.vm.sade.kayttooikeus.controller.KutsuPopulator.kutsu;
import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.palvelukayttaja;
import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.virkailija;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusPopulator.oikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaMyontoViitePopulator.kayttoOikeusRyhmaMyontoViite;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.KutsuOrganisaatioPopulator.kutsuOrganisaatio;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloKayttoOikeusPopulator.myonnettyKayttoOikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.TextGroupPopulator.text;
import static fi.vm.sade.kayttooikeus.service.impl.PermissionCheckerServiceImpl.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@WithMockUser
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class KutsuServiceTest extends AbstractServiceIntegrationTest {
    @Autowired
    private KutsuService kutsuService;

    @MockitoSpyBean
    private KayttooikeusAnomusService kayttooikeusAnomusService;

    @MockitoBean
    private OrganisaatioClient organisaatioClient;

    @MockitoBean
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @MockitoBean
    private HenkiloHelper henkiloHelper;

    @MockitoBean
    private OrganisaatioService organisaatioService;

    @MockitoBean
    private EmailService emailService;

    @Test
    @WithMockUser(username = "1.2.4", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void listAvoinKutsus() {
        populate(myonnettyKayttoOikeus(organisaatioHenkilo("1.2.3", "1.2.3.4.5"),
                kayttoOikeusRyhma("RYHMA1").withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD))));
        populate(myonnettyKayttoOikeus(organisaatioHenkilo("1.2.4", "1.2.3.4.5"),
                kayttoOikeusRyhma("RYHMA1").withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD))));
        populate(myonnettyKayttoOikeus(organisaatioHenkilo("1.2.4", "1.2.3.4.6"),
                kayttoOikeusRyhma("RYHMA1").withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD))));
        given(this.organisaatioClient.listWithChildOids(eq("1.2.3.4.5"), any())).willReturn(singleton("1.2.3.4.5"));
        given(this.organisaatioClient.listWithChildOids(eq("1.2.3.4.6"), any())).willReturn(singleton("1.2.3.4.6"));
        populate(kutsu("Essi", "Esimerkki", "a@eaxmple.com")
                .kutsuja("1.2.3").aikaleima(LocalDateTime.of(2016, 1, 1, 0, 0, 0, 0))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                        .ryhma(kayttoOikeusRyhma("RYHMA1"))
                ));
        Kutsu kutsu2 = populate(kutsu("Matti", "Meikäläinen", "b@eaxmple.com")
                .kutsuja("1.2.4").aikaleima(LocalDateTime.of(2016, 2, 1, 0, 0, 0, 0))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                        .ryhma(kayttoOikeusRyhma("RYHMA2")))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.6")
                        .ryhma(kayttoOikeusRyhma("RYHMA3")))
        );
        populate(kutsu("Eero", "Esimerkki", "c@eaxmple.com")
                .tila(KutsunTila.POISTETTU)
                .kutsuja("1.2.4").aikaleima(LocalDateTime.of(2016, 1, 1, 0, 0, 0, 0))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5").ryhma(kayttoOikeusRyhma("RYHMA1"))
                ));

        OrganisaatioPerustieto org1 = new OrganisaatioPerustieto();
        org1.setOid("1.2.3.4.5");
        org1.setNimi(new TextGroupMapDto().put("fi", "Nimi2").asMap());
        OrganisaatioPerustieto org2 = new OrganisaatioPerustieto();
        org2.setOid("1.2.3.4.6");
        org2.setNimi(new TextGroupMapDto().put("fi", "Nimi1").asMap());
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.5")))
                .willReturn(Optional.of(org1));
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.6")))
                .willReturn(Optional.of(org2));

        List<KutsuReadDto> kutsus = kutsuService.listKutsus(KutsuOrganisaatioOrder.AIKALEIMA, Sort.Direction.ASC, KutsuCriteria.builder().searchTerm("matti meikäläinen").build(), null, null);
        assertEquals(1, kutsus.size());
        assertEquals(LocalDateTime.of(2016, 2, 1, 0, 0, 0, 0), kutsus.get(0).getAikaleima());
        assertEquals(kutsu2.getId(), kutsus.get(0).getId());
        assertEquals("b@eaxmple.com", kutsus.get(0).getSahkoposti());
        assertEquals(2, kutsus.get(0).getOrganisaatiot().size());
        assertThat(kutsus).flatExtracting(KutsuReadDto::getOrganisaatiot)
                .extracting(KutsuReadDto.KutsuOrganisaatioReadDto::getOrganisaatioOid)
                .containsExactlyInAnyOrder("1.2.3.4.5", "1.2.3.4.6");
        assertThat(kutsus).extracting(KutsuReadDto::getEtunimi).containsExactlyInAnyOrder("Matti");
        assertThat(kutsus).extracting(KutsuReadDto::getSukunimi).containsExactlyInAnyOrder("Meikäläinen");
        assertThat(kutsus).flatExtracting(KutsuReadDto::getOrganisaatiot)
                .extracting(KutsuReadDto.KutsuOrganisaatioReadDto::getNimi)
                .extracting(TextGroupMapDto::getTexts)
                .extracting(map -> map.get("fi"))
                .containsExactlyInAnyOrder("Nimi1", "Nimi2");
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = {"ROLE_APP_KAYTTOOIKEUS_CRUD", "ROLE_APP_KAYTTOOIKEUS_CRUD_1.2.246.562.10.00000000001"})
    public void listAvoinKutsusWithMiniAdminAndOrganisationIsForcedWithOphView() {
        populate(myonnettyKayttoOikeus(organisaatioHenkilo("1.2.3", "1.2.246.562.10.00000000001"),
                kayttoOikeusRyhma("RYHMA1").withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD))));
        populate(kutsu("Essi", "Esimerkki", "a@eaxmple.com")
                .kutsuja("1.2.3").aikaleima(LocalDateTime.of(2016, 1, 1, 0, 0, 0, 0))
                .organisaatio(kutsuOrganisaatio("1.2.246.562.10.00000000001")
                        .ryhma(kayttoOikeusRyhma("RYHMA1").withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD)))));
        given(this.organisaatioClient.listWithChildOids(eq("1.2.246.562.10.00000000001"), any())).willReturn(singleton("1.2.246.562.10.00000000001"));
        OrganisaatioPerustieto org1 = new OrganisaatioPerustieto();
        org1.setOid("1.2.246.562.10.00000000001");
        org1.setNimi(new TextGroupMapDto().put("fi", "Nimi2").asMap());
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.246.562.10.00000000001")))
                .willReturn(Optional.of(org1));

        // Does not allow changing organisaatio with ophView
        List<KutsuReadDto> kutsuList = this.kutsuService.listKutsus(KutsuOrganisaatioOrder.AIKALEIMA,
                Sort.Direction.ASC,
                KutsuCriteria.builder().kutsujaOrganisaatioOid("1.2.3.4.5").view(KutsuView.OPH).build(),
                null,
                null);
        assertThat(kutsuList)
                .flatExtracting(KutsuReadDto::getOrganisaatiot)
                .flatExtracting(KutsuReadDto.KutsuOrganisaatioReadDto::getOrganisaatioOid)
                .containsExactly("1.2.246.562.10.00000000001");
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = {"ROLE_APP_KAYTTOOIKEUS_CRUD", "ROLE_APP_KAYTTOOIKEUS_CRUD_1.2.3.4.5"})
    public void listAvoinKutsusWithMiniAdminAndKayttooikeusryhmaView() {
        MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma = populate(
                myonnettyKayttoOikeus(organisaatioHenkilo("1.2.3", "1.2.3.4.5"),
                        kayttoOikeusRyhma("RYHMA1").withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD))));
        populate(kutsu("Essi", "Esimerkki", "a@eaxmple.com")
                .kutsuja("1.2.3").aikaleima(LocalDateTime.of(2016, 1, 1, 0, 0, 0, 0))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                        .ryhma(kayttoOikeusRyhma("RYHMA1").withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD)))));
        OrganisaatioPerustieto org1 = new OrganisaatioPerustieto();
        org1.setOid("1.2.3.4.5");
        org1.setNimi(new TextGroupMapDto().put("fi", "Nimi2").asMap());
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.5")))
                .willReturn(Optional.of(org1));
        given(this.organisaatioClient.listWithChildOids(eq("1.2.3.4.5"), any())).willReturn(singleton("1.2.3.4.5"));

        List<KutsuReadDto> kutsuList = this.kutsuService.listKutsus(KutsuOrganisaatioOrder.AIKALEIMA,
                Sort.Direction.ASC,
                KutsuCriteria.builder().kutsujaKayttooikeusryhmaIds(Sets.newHashSet(999L)).view(KutsuView.KAYTTOOIKEUSRYHMA).build(),
                null,
                null);
        assertThat(kutsuList)
                .flatExtracting(KutsuReadDto::getOrganisaatiot)
                .flatExtracting(KutsuReadDto.KutsuOrganisaatioReadDto::getKayttoOikeusRyhmat)
                .extracting(KutsuReadDto.KutsuKayttoOikeusRyhmaReadDto::getId)
                .containsExactly(myonnettyKayttoOikeusRyhmaTapahtuma.getKayttoOikeusRyhma().getId());
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = {"ROLE_APP_KAYTTOOIKEUS_CRUD", "ROLE_APP_KAYTTOOIKEUS_CRUD_1.2.3.4.5"})
    public void listAvoinKutsusWithNormalUserAndOrganisationIsForced() {
        populate(myonnettyKayttoOikeus(organisaatioHenkilo("1.2.3", "1.2.3.4.5"),
                kayttoOikeusRyhma("RYHMA1").withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD))));
        populate(myonnettyKayttoOikeus(organisaatioHenkilo("1.2.4", "1.2.3.4.5"),
                kayttoOikeusRyhma("RYHMA1").withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD))));
        populate(kutsu("Essi", "Esimerkki", "a@eaxmple.com")
                .kutsuja("1.2.3").aikaleima(LocalDateTime.of(2016, 1, 1, 0, 0, 0, 0))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                        .ryhma(kayttoOikeusRyhma("RYHMA1").withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD)))));
        populate(kutsu("Essi", "Esimerkki", "a@eaxmple.com")
                .kutsuja("1.2.3").aikaleima(LocalDateTime.of(2016, 1, 1, 0, 0, 0, 0))
                .organisaatio(kutsuOrganisaatio("1.2.246.562.10.00000000001")
                        .ryhma(kayttoOikeusRyhma("RYHMA1").withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD)))));
        given(this.organisaatioClient.listWithChildOids(eq("1.2.246.562.10.00000000001"), any())).willReturn(singleton("1.2.246.562.10.00000000001"));
        given(this.organisaatioClient.listWithChildOids(eq("1.2.3.4.5"), any())).willReturn(singleton("1.2.3.4.5"));
        OrganisaatioPerustieto org = new OrganisaatioPerustieto();
        org.setOid("1.2.3.4.5");
        org.setNimi(new TextGroupMapDto().put("fi", "Nimi2").asMap());
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.5")))
                .willReturn(Optional.of(org));

        List<KutsuReadDto> kutsuList = this.kutsuService.listKutsus(KutsuOrganisaatioOrder.AIKALEIMA,
                Sort.Direction.ASC,
                KutsuCriteria.builder().build(),
                null,
                null);
        assertThat(kutsuList)
                .flatExtracting(KutsuReadDto::getOrganisaatiot)
                .flatExtracting(KutsuReadDto.KutsuOrganisaatioReadDto::getOrganisaatioOid)
                .containsExactly("1.2.3.4.5");
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = {"ROLE_APP_KAYTTOOIKEUS_CRUD", "ROLE_APP_KAYTTOOIKEUS_CRUD_1.2.3.4.5"})
    public void listAvoinKutsusWithNormalUserByKayttooikeusryhmaId() {
        MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma
                = populate(myonnettyKayttoOikeus(organisaatioHenkilo("1.2.4", "1.2.3.4.5"),
                kayttoOikeusRyhma("RYHMA1").withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD))));
        populate(myonnettyKayttoOikeus(organisaatioHenkilo("kutsujaOid", "1.2.3.4.5"),
                kayttoOikeusRyhma("RYHMA1").withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD))));
        populate(kutsu("Essi", "Esimerkki", "a@eaxmple.com")
                .kutsuja("kutsujaOid").aikaleima(LocalDateTime.of(2016, 1, 1, 0, 0, 0, 0))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                        .ryhma(kayttoOikeusRyhma("RYHMA1").withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD)))));
        populate(kutsu("Essi", "Esimerkki", "a@eaxmple.com")
                .kutsuja("kutsujaOid").aikaleima(LocalDateTime.of(2016, 1, 1, 0, 0, 0, 0))
                .organisaatio(kutsuOrganisaatio("1.2.246.562.10.00000000001")
                        .ryhma(kayttoOikeusRyhma("RYHMA1").withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD)))));
        given(this.organisaatioClient.listWithChildOids(eq("1.2.3.4.5"), any())).willReturn(singleton("1.2.3.4.5"));
        OrganisaatioPerustieto org = new OrganisaatioPerustieto();
        org.setOid("1.2.3.4.5");
        org.setNimi(new TextGroupMapDto().put("fi", "Nimi2").asMap());
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.5")))
                .willReturn(Optional.of(org));

        // Ryhmä user has not rights to will be set to all his ryhmas
        List<KutsuReadDto> kutsuList = this.kutsuService.listKutsus(KutsuOrganisaatioOrder.AIKALEIMA,
                Sort.Direction.ASC,
                KutsuCriteria.builder().kayttooikeusryhmaIds(Sets.newHashSet(myonnettyKayttoOikeusRyhmaTapahtuma.getKayttoOikeusRyhma().getId())).build(),
                null,
                null);
        assertThat(kutsuList)
                .flatExtracting(KutsuReadDto::getOrganisaatiot)
                .flatExtracting(KutsuReadDto.KutsuOrganisaatioReadDto::getOrganisaatioOid)
                .containsExactly("1.2.3.4.5");
        assertThat(kutsuList)
                .flatExtracting(KutsuReadDto::getOrganisaatiot)
                .flatExtracting(KutsuReadDto.KutsuOrganisaatioReadDto::getKayttoOikeusRyhmat)
                .extracting(KutsuReadDto.KutsuKayttoOikeusRyhmaReadDto::getId)
                .containsExactly(myonnettyKayttoOikeusRyhmaTapahtuma.getKayttoOikeusRyhma().getId());
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = {"ROLE_APP_KAYTTOOIKEUS_CRUD", "ROLE_APP_KAYTTOOIKEUS_CRUD_1.2.246.562.10.2"})
    public void listKutsusPassivoituOrganisaatioNormaaliVirkailija() {
        HenkiloPopulator kutsuja = HenkiloPopulator.henkilo("kutsujaOid");
        populate(myonnettyKayttoOikeus(organisaatioHenkilo(kutsuja, "1.2.246.562.10.1").passivoitu(), kayttoOikeusRyhma("käyttöoikeusryhmä1")));
        populate(myonnettyKayttoOikeus(organisaatioHenkilo(kutsuja, "1.2.246.562.10.2"), kayttoOikeusRyhma("käyttöoikeusryhmä2")));
        populate(kutsu("Essi", "Esimerkki", "a@example.com").kutsuja("kutsujaOid").organisaatio(kutsuOrganisaatio("1.2.246.562.10.1")));

        KutsuOrganisaatioOrder order = KutsuOrganisaatioOrder.AIKALEIMA;
        Sort.Direction direction = Sort.Direction.ASC;
        KutsuCriteria criteria = new KutsuCriteria();

        List<KutsuReadDto> kutsuList = kutsuService.listKutsus(order, direction, criteria, null, null);

        assertThat(kutsuList).isEmpty();
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = {"ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA", "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA_1.2.246.562.10.00000000001"})
    public void listKutsusPassivoituOrganisaatioRekisterinpitaja() {
        HenkiloPopulator kutsuja = HenkiloPopulator.henkilo("kutsujaOid");
        populate(myonnettyKayttoOikeus(organisaatioHenkilo(kutsuja, "1.2.246.562.10.1").passivoitu(), kayttoOikeusRyhma("käyttöoikeusryhmä1")));
        Kutsu kutsu = populate(kutsu("Essi", "Esimerkki", "a@example.com").kutsuja("kutsujaOid").organisaatio(kutsuOrganisaatio("1.2.246.562.10.1")));

        KutsuOrganisaatioOrder order = KutsuOrganisaatioOrder.AIKALEIMA;
        Sort.Direction direction = Sort.Direction.ASC;
        KutsuCriteria criteria = new KutsuCriteria();

        List<KutsuReadDto> kutsuList = kutsuService.listKutsus(order, direction, criteria, null, null);

        assertThat(kutsuList).extracting(KutsuReadDto::getId).containsExactly(kutsu.getId());
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = {"ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA", "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA_1.2.246.562.10.00000000001"})
    public void createKutsuAsAdmin() {
        doReturn(HenkiloDto.builder()
                .kutsumanimi("kutsun")
                .sukunimi("kutsuja")
                .yksiloityVTJ(true)
                .hetu("valid hetu")
                .build())
                .when(this.oppijanumerorekisteriClient).getHenkiloByOid(anyString());

        OrganisaatioPerustieto org1 = new OrganisaatioPerustieto();
        org1.setOid("1.2.246.562.10.00000000001");
        org1.setNimi(new TextGroupMapDto().put("FI", "Opetushallitus").asMap());
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.246.562.10.00000000001")))
                .willReturn(Optional.of(org1));

        MyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(virkailija("1.2.4"), "1.2.246.562.10.00000000001"),
                kayttoOikeusRyhma("kayttoOikeusRyhma")
                        .withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD))
                        .withNimi(text("fi", "Käyttöoikeusryhmä"))));
        Long kayttoOikeusRyhmaId = tapahtuma.getKayttoOikeusRyhma().getId();
        KutsuCreateDto.KutsuKayttoOikeusRyhmaCreateDto kutsuKayttoOikeusRyhma = new KutsuCreateDto.KutsuKayttoOikeusRyhmaCreateDto();
        kutsuKayttoOikeusRyhma.setId(kayttoOikeusRyhmaId);

        KutsuCreateDto kutsu = new KutsuCreateDto();
        kutsu.setEtunimi("Etu");
        kutsu.setSukunimi("Suku");
        kutsu.setSahkoposti("example@example.com");
        kutsu.setAsiointikieli(Asiointikieli.fi);
        kutsu.setOrganisaatiot(new LinkedHashSet<>());
        KutsuCreateDto.KutsuOrganisaatioCreateDto kutsuOrganisaatio = new KutsuCreateDto.KutsuOrganisaatioCreateDto();
        kutsuOrganisaatio.setOrganisaatioOid("1.2.246.562.10.00000000001");
        kutsuOrganisaatio.setKayttoOikeusRyhmat(Stream.of(kutsuKayttoOikeusRyhma).collect(toSet()));
        kutsu.getOrganisaatiot().add(kutsuOrganisaatio);

        long id = kutsuService.createKutsu(kutsu);
        Kutsu tallennettu = em.find(Kutsu.class, id);
        assertThat(tallennettu.getKieliKoodi()).isEqualTo("fi");
        assertThat(tallennettu.getOrganisaatiot())
                .hasSize(1)
                .flatExtracting(KutsuOrganisaatio::getRyhmat)
                .hasSize(1);
        assertThat(tallennettu.getSalaisuus()).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = {"ROLE_APP_KAYTTOOIKEUS_CRUD", "ROLE_APP_KAYTTOOIKEUS_CRUD_1.2.3.4.5"})
    public void createKutsuAsNormalUser() {
        doReturn(HenkiloDto.builder()
                .kutsumanimi("kutsun")
                .sukunimi("kutsuja")
                .yksiloityVTJ(true)
                .hetu("valid hetu")
                .build())
                .when(this.oppijanumerorekisteriClient).getHenkiloByOid(anyString());

        OrganisaatioPerustieto org1 = new OrganisaatioPerustieto();
        org1.setOid("1.2.3.4.1");
        org1.setNimi(new TextGroupMapDto().put("FI", "Kutsuttu organisaatio").asMap());
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.1")))
                .willReturn(Optional.of(org1));
        given(this.organisaatioClient.listWithParentsAndChildren(eq("1.2.3.4.1"), any()))
                .willReturn(asList(org1));
        OrganisaatioPerustieto org2 = new OrganisaatioPerustieto();
        org2.setOid("1.2.3.4.5");
        org2.setNimi(new TextGroupMapDto().put("FI", "Käyttäjän organisaatio").asMap());
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.5")))
                .willReturn(Optional.of(org2));
        given(this.organisaatioClient.listWithParentsAndChildren(eq("1.2.3.4.5"), any()))
                .willReturn(asList(org2));

        MyonnettyKayttoOikeusRyhmaTapahtuma myonnetty = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(virkailija("1.2.4"), "1.2.3.4.5"),
                kayttoOikeusRyhma("kayttoOikeusRyhma")
                        .withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD))));
        KayttoOikeusRyhma myonnettava = populate(kayttoOikeusRyhma("RYHMA2")
                .withOrganisaatiorajoite("1.2.3.4.1")
                .withNimi(text("fi", "Käyttöoikeusryhmä")));
        populate(kayttoOikeusRyhmaMyontoViite(myonnetty.getKayttoOikeusRyhma().getId(),
                myonnettava.getId()));

        KutsuCreateDto.KutsuKayttoOikeusRyhmaCreateDto kutsuKayttoOikeusRyhma = new KutsuCreateDto.KutsuKayttoOikeusRyhmaCreateDto();
        kutsuKayttoOikeusRyhma.setId(myonnettava.getId());
        given(this.organisaatioClient.listWithChildOids(eq("1.2.3.4.5"), any()))
                .willReturn(Stream.of("1.2.3.4.5", "1.2.3.4.1").collect(toSet()));

        KutsuCreateDto kutsu = new KutsuCreateDto();
        kutsu.setEtunimi("Etu");
        kutsu.setSukunimi("Suku");
        kutsu.setSahkoposti("example@example.com");
        kutsu.setAsiointikieli(Asiointikieli.fi);
        kutsu.setOrganisaatiot(new LinkedHashSet<>());
        KutsuCreateDto.KutsuOrganisaatioCreateDto kutsuOrganisaatio = new KutsuCreateDto.KutsuOrganisaatioCreateDto();
        kutsuOrganisaatio.setOrganisaatioOid("1.2.3.4.1");
        kutsuOrganisaatio.setKayttoOikeusRyhmat(Stream.of(kutsuKayttoOikeusRyhma).collect(toSet()));
        kutsu.getOrganisaatiot().add(kutsuOrganisaatio);

        long id = kutsuService.createKutsu(kutsu);
        Kutsu tallennettu = this.em.find(Kutsu.class, id);

        assertThat(tallennettu.getKieliKoodi()).isEqualTo("fi");
        assertThat(tallennettu.getOrganisaatiot())
                .hasSize(1)
                .flatExtracting(KutsuOrganisaatio::getRyhmat)
                .hasSize(1);
        assertThat(tallennettu.getSalaisuus()).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = {"ROLE_APP_KAYTTOOIKEUS_KUTSU_CRUD", "ROLE_APP_KAYTTOOIKEUS_KUTSU_CRUD_1.2.3.4.5"})
    public void createKutsuAsNormalUserWithKutsuCrud() {
        doReturn(HenkiloDto.builder()
                .kutsumanimi("kutsun")
                .sukunimi("kutsuja")
                .yksiloityVTJ(true)
                .hetu("valid hetu")
                .build())
                .when(this.oppijanumerorekisteriClient).getHenkiloByOid(anyString());

        OrganisaatioPerustieto org1 = new OrganisaatioPerustieto();
        org1.setOid("1.2.3.4.1");
        org1.setNimi(new TextGroupMapDto().put("FI", "Kutsuttu organisaatio").asMap());
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.1")))
                .willReturn(Optional.of(org1));
        given(this.organisaatioClient.listWithParentsAndChildren(eq("1.2.3.4.1"), any()))
                .willReturn(asList(org1));
        OrganisaatioPerustieto org2 = new OrganisaatioPerustieto();
        org2.setOid("1.2.3.4.5");
        org2.setNimi(new TextGroupMapDto().put("FI", "Käyttäjän organisaatio").asMap());
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.5")))
                .willReturn(Optional.of(org2));
        given(this.organisaatioClient.listWithParentsAndChildren(eq("1.2.3.4.5"), any()))
                .willReturn(asList(org2));

        MyonnettyKayttoOikeusRyhmaTapahtuma myonnetty = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(virkailija("1.2.4"), "1.2.3.4.5"),
                kayttoOikeusRyhma("kayttoOikeusRyhma")
                        .withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_KUTSU_CRUD))));
        KayttoOikeusRyhma myonnettava = populate(kayttoOikeusRyhma("RYHMA2")
                .withOrganisaatiorajoite("1.2.3.4.1")
                .withNimi(text("fi", "Käyttöoikeusryhmä")));
        populate(kayttoOikeusRyhmaMyontoViite(myonnetty.getKayttoOikeusRyhma().getId(),
                myonnettava.getId()));

        KutsuCreateDto.KutsuKayttoOikeusRyhmaCreateDto kutsuKayttoOikeusRyhma = new KutsuCreateDto.KutsuKayttoOikeusRyhmaCreateDto();
        kutsuKayttoOikeusRyhma.setId(myonnettava.getId());
        given(this.organisaatioClient.listWithChildOids(eq("1.2.3.4.5"), any()))
                .willReturn(Stream.of("1.2.3.4.5", "1.2.3.4.1").collect(toSet()));

        KutsuCreateDto kutsu = new KutsuCreateDto();
        kutsu.setEtunimi("Etu");
        kutsu.setSukunimi("Suku");
        kutsu.setSahkoposti("example@example.com");
        kutsu.setAsiointikieli(Asiointikieli.fi);
        kutsu.setOrganisaatiot(new LinkedHashSet<>());
        KutsuCreateDto.KutsuOrganisaatioCreateDto kutsuOrganisaatio = new KutsuCreateDto.KutsuOrganisaatioCreateDto();
        kutsuOrganisaatio.setOrganisaatioOid("1.2.3.4.1");
        kutsuOrganisaatio.setKayttoOikeusRyhmat(Stream.of(kutsuKayttoOikeusRyhma).collect(toSet()));
        kutsu.getOrganisaatiot().add(kutsuOrganisaatio);

        long id = kutsuService.createKutsu(kutsu);
        Kutsu tallennettu = this.em.find(Kutsu.class, id);

        assertThat(tallennettu.getKieliKoodi()).isEqualTo("fi");
        assertThat(tallennettu.getOrganisaatiot())
                .hasSize(1)
                .flatExtracting(KutsuOrganisaatio::getRyhmat)
                .hasSize(1);
        assertThat(tallennettu.getSalaisuus()).isNotEmpty();

        verify(emailService, times(1)).sendInvitationEmail(any(Kutsu.class), eq(Optional.empty()));
    }

    @Test
    @WithMockUser(username = "1.2.3", authorities = {"ROLE_APP_KAYTTOOIKEUS_KUTSU_CRUD", "ROLE_APP_KAYTTOOIKEUS_KUTSU_CRUD_1.2.3.4.5"})
    public void createKutsuAsPalvelukayttaja() {
        final String kutsujaForEmail = "makkara";
        doReturn(HenkiloDto.builder()
                .kutsumanimi("kutsun")
                .sukunimi("kutsuja")
                .yksiloityVTJ(true)
                .hetu("valid hetu")
                .build())
                .when(this.oppijanumerorekisteriClient).getHenkiloByOid(anyString());

        OrganisaatioPerustieto org1 = new OrganisaatioPerustieto();
        org1.setOid("1.2.3.4.1");
        org1.setNimi(new TextGroupMapDto().put("FI", "Kutsuttu organisaatio").asMap());
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.1")))
                .willReturn(Optional.of(org1));
        given(this.organisaatioClient.listWithParentsAndChildren(eq("1.2.3.4.1"), any()))
                .willReturn(asList(org1));
        OrganisaatioPerustieto org2 = new OrganisaatioPerustieto();
        org2.setOid("1.2.3.4.5");
        org2.setNimi(new TextGroupMapDto().put("FI", "Käyttäjän organisaatio").asMap());
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.5")))
                .willReturn(Optional.of(org2));
        given(this.organisaatioClient.listWithParentsAndChildren(eq("1.2.3.4.5"), any()))
                .willReturn(asList(org2));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(palvelukayttaja("1.2.3"), "1.2.3.4.5"),
                kayttoOikeusRyhma("RYHMA3")
                        .withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_KUTSU_CRUD))));
        MyonnettyKayttoOikeusRyhmaTapahtuma myonnetty = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(virkailija("1.2.4"), "1.2.3.4.5"),
                kayttoOikeusRyhma("kayttoOikeusRyhma")
                        .withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD))));
        KayttoOikeusRyhma myonnettava = populate(kayttoOikeusRyhma("RYHMA2")
                .withOrganisaatiorajoite("1.2.3.4.1")
                .withNimi(text("fi", "Käyttöoikeusryhmä")));
        populate(kayttoOikeusRyhmaMyontoViite(myonnetty.getKayttoOikeusRyhma().getId(),
                myonnettava.getId()));

        KutsuCreateDto.KutsuKayttoOikeusRyhmaCreateDto kutsuKayttoOikeusRyhma = new KutsuCreateDto.KutsuKayttoOikeusRyhmaCreateDto();
        kutsuKayttoOikeusRyhma.setId(myonnettava.getId());
        given(this.organisaatioClient.getActiveParentOids(eq("1.2.3.4.1")))
                .willReturn(asList("1.2.3.4.1", "1.2.3.4.5"));
        given(this.organisaatioClient.listWithChildOids(eq("1.2.3.4.5"), any()))
                .willReturn(Stream.of("1.2.3.4.5", "1.2.3.4.1").collect(toSet()));

        KutsuCreateDto kutsu = new KutsuCreateDto();
        kutsu.setKutsujaOid("1.2.4");
        kutsu.setEtunimi("Etu");
        kutsu.setSukunimi("Suku");
        kutsu.setSahkoposti("example@example.com");
        kutsu.setAsiointikieli(Asiointikieli.fi);
        kutsu.setOrganisaatiot(new LinkedHashSet<>());
        KutsuCreateDto.KutsuOrganisaatioCreateDto kutsuOrganisaatio = new KutsuCreateDto.KutsuOrganisaatioCreateDto();
        kutsuOrganisaatio.setOrganisaatioOid("1.2.3.4.1");
        kutsuOrganisaatio.setKayttoOikeusRyhmat(Stream.of(kutsuKayttoOikeusRyhma).collect(toSet()));
        kutsu.getOrganisaatiot().add(kutsuOrganisaatio);
        kutsu.setKutsujaForEmail(kutsujaForEmail);

        long id = kutsuService.createKutsu(kutsu);
        Kutsu tallennettu = this.em.find(Kutsu.class, id);

        assertThat(tallennettu)
                .returns("fi", Kutsu::getKieliKoodi)
                .returns("1.2.4", Kutsu::getKutsuja);
        assertThat(tallennettu.getOrganisaatiot())
                .hasSize(1)
                .flatExtracting(KutsuOrganisaatio::getRyhmat)
                .hasSize(1);
        assertThat(tallennettu.getSalaisuus()).isNotEmpty();

        verify(emailService, times(1)).sendInvitationEmail(any(), eq(Optional.of(kutsujaForEmail)));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.6", authorities = {"ROLE_APP_KAYTTOOIKEUS_KUTSU_CRUD", "ROLE_APP_KAYTTOOIKEUS_KUTSU_CRUD_1.2.3.4.6"})
    public void createKutsuAsPalvelukayttajaOnAllowlist() {
        final String kutsujaForEmail = "makkara";

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(palvelukayttaja("1.2.3.4.6"), "1.2.3.4.5"),
                kayttoOikeusRyhma("RYHMA3")
                        .withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_KUTSU_CRUD))));
        MyonnettyKayttoOikeusRyhmaTapahtuma myonnetty = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(virkailija("1.2.4"), "1.2.3.4.5"),
                kayttoOikeusRyhma("kayttoOikeusRyhma")
                        .withOikeus(oikeus(PALVELU_KAYTTOOIKEUS, ROLE_CRUD))));
        KayttoOikeusRyhma myonnettava = populate(kayttoOikeusRyhma("RYHMA2")
                .withOrganisaatiorajoite("1.2.3.4.1")
                .withNimi(text("fi", "Käyttöoikeusryhmä")));
        populate(kayttoOikeusRyhmaMyontoViite(myonnetty.getKayttoOikeusRyhma().getId(),
                myonnettava.getId()));

        KutsuCreateDto.KutsuKayttoOikeusRyhmaCreateDto kutsuKayttoOikeusRyhma = new KutsuCreateDto.KutsuKayttoOikeusRyhmaCreateDto();
        kutsuKayttoOikeusRyhma.setId(myonnettava.getId());
        given(this.organisaatioClient.getActiveParentOids(eq("1.2.3.4.1")))
                .willReturn(asList("1.2.3.4.1", "1.2.3.4.5"));
        given(this.organisaatioClient.listWithChildOids(eq("1.2.3.4.5"), any()))
                .willReturn(Stream.of("1.2.3.4.5", "1.2.3.4.1").collect(toSet()));

        KutsuCreateDto kutsu = new KutsuCreateDto();
        kutsu.setKutsujaOid("1.2.4");
        kutsu.setEtunimi("Etu");
        kutsu.setSukunimi("Suku");
        kutsu.setSahkoposti("example@example.com");
        kutsu.setAsiointikieli(Asiointikieli.fi);
        kutsu.setOrganisaatiot(new LinkedHashSet<>());
        KutsuCreateDto.KutsuOrganisaatioCreateDto kutsuOrganisaatio = new KutsuCreateDto.KutsuOrganisaatioCreateDto();
        kutsuOrganisaatio.setOrganisaatioOid("1.2.3.4.1");
        kutsuOrganisaatio.setKayttoOikeusRyhmat(Stream.of(kutsuKayttoOikeusRyhma).collect(toSet()));
        kutsu.getOrganisaatiot().add(kutsuOrganisaatio);
        kutsu.setKutsujaForEmail(kutsujaForEmail);

        long id = kutsuService.createKutsu(kutsu);
        Kutsu tallennettu = this.em.find(Kutsu.class, id);

        assertThat(tallennettu)
                .returns("fi", Kutsu::getKieliKoodi)
                .returns("1.2.4", Kutsu::getKutsuja);
        assertThat(tallennettu.getOrganisaatiot())
                .hasSize(1)
                .flatExtracting(KutsuOrganisaatio::getRyhmat)
                .hasSize(1);
        assertThat(tallennettu.getSalaisuus()).isNotEmpty();

        verify(emailService, times(1)).sendInvitationEmail(any(), eq(Optional.of(kutsujaForEmail)));
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = {"ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA", "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA_1.2.246.562.10.00000000001"})
    public void createKutsuAsAdminWithNoHetuOrVtjYksiloity() {
        assertThrows(ForbiddenException.class, () -> {
            doReturn(HenkiloDto.builder()
                    .kutsumanimi("kutsun")
                    .sukunimi("kutsuja")
                    .build())
                    .when(this.oppijanumerorekisteriClient).getHenkiloByOid(anyString());
            populate(virkailija("1.2.4"));
            // This kind of kutsu is not actually allowed on api.
            this.kutsuService.createKutsu(KutsuCreateDto.builder().organisaatiot(new HashSet<>()).build());
        });
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = {"ROLE_APP_KAYTTOOIKEUS_CRUD", "ROLE_APP_KAYTTOOIKEUS_CRUD_1.2.3.4.5"})
    public void deleteKutsuTest() {
        Kutsu kutsu = populate(kutsu("Matti", "Mehiläinen", "b@eaxmple.com")
                .kutsuja("1.2.4")
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                        .ryhma(kayttoOikeusRyhma("RYHMA2")))
        );
        populate(organisaatioHenkilo("1.2.4", "1.2.3.4.5"));
        given(this.organisaatioClient.listWithChildOids(eq("1.2.3.4.5"), any())).willReturn(singleton("1.2.3.4.5"));
        this.kutsuService.deleteKutsu(kutsu.getId());
        this.em.flush();
        assertEquals(KutsunTila.POISTETTU, kutsu.getTila());
        assertEquals("1.2.4", kutsu.getPoistaja());
        assertNotNull(kutsu.getPoistettu());
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void deleteKutsuOtherKutsujaWithoutProperAuthorityFails() {
        assertThrows(ForbiddenException.class, () -> {
            Kutsu kutsu = populate(kutsu("Matti", "Mehiläinen", "b@eaxmple.com")
                    .kutsuja("1.2.5")
                    .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                            .ryhma(kayttoOikeusRyhma("RYHMA2")))
            );
            this.kutsuService.deleteKutsu(kutsu.getId());
        });
    }

    @Test
    public void findExpiredInvitations() {
        assertThat(kutsuService.findExpired(Period.ZERO)).isEmpty();
    }

    @Test
    public void discardInvitation() {
        Kutsu invitation = Mockito.mock(Kutsu.class);

        kutsuService.discard(invitation);

        verify(invitation, times(1)).poista(anyString());
    }
}
