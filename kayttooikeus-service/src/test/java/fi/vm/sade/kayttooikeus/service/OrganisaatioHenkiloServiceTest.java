package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloWithOrganisaatioDto.OrganisaatioDto;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloRepository;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import fi.vm.sade.kayttooikeus.service.it.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila.SULJETTU;
import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.MyonnettyKayttooikeusRyhmaTapahtumaPopulator.kayttooikeusTapahtuma;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static fi.vm.sade.kayttooikeus.service.impl.PermissionCheckerServiceImpl.PALVELU_HENKILONHALLINTA;
import static fi.vm.sade.kayttooikeus.service.impl.PermissionCheckerServiceImpl.ROLE_ADMIN;
import static fi.vm.sade.kayttooikeus.service.impl.PermissionCheckerServiceImpl.ROLE_CRUD;
import static fi.vm.sade.kayttooikeus.util.JsonUtil.readJson;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloTyyppi;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
public class OrganisaatioHenkiloServiceTest extends AbstractServiceIntegrationTest {
    @MockBean
    private OrganisaatioClient organisaatioClient;

    @MockBean
    private OrganisaatioHenkiloRepository organisaatioHenkiloRepository;

    @MockBean
    private KayttoOikeusRepository kayttoOikeusRepository;

    @Autowired
    private OrganisaatioHenkiloService organisaatioHenkiloService;

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void listOrganisaatioHenkilosTest() {
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.1"))).willAnswer(invocation -> {
            OrganisaatioPerustieto orgDto = new OrganisaatioPerustieto();
            orgDto.setOid("1.2.3.4.1");
            orgDto.setNimi(new TextGroupMapDto().put("fi", "Suomeksi").put("en", "In English").asMap());
            orgDto.setOrganisaatiotyypit(asList("Tyyppi1", "Tyyppi2"));
            return Optional.of(orgDto);
        });
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.2"))).willAnswer(invocation -> {
            OrganisaatioPerustieto orgDto = new OrganisaatioPerustieto();
            orgDto.setOid("1.2.3.4.2");
            orgDto.setNimi(new TextGroupMapDto().put("en", "Only in English").asMap());
            orgDto.setOrganisaatiotyypit(singletonList("Tyyppi1"));
            return Optional.of(orgDto);
        });
        given(this.organisaatioHenkiloRepository.findActiveOrganisaatioHenkiloListDtos("1.2.3.4.5")).willReturn(
                asList(OrganisaatioHenkiloWithOrganisaatioDto.organisaatioBuilder().id(1L).passivoitu(false)
                                .voimassaAlkuPvm(LocalDate.now()).voimassaLoppuPvm(LocalDate.now().plusYears(1))
                                .tehtavanimike("Devaaja")
                                .organisaatio(OrganisaatioDto.builder().oid("1.2.3.4.1").build()).build(),
                        OrganisaatioHenkiloWithOrganisaatioDto.organisaatioBuilder().id(2L).voimassaAlkuPvm(LocalDate.now().minusYears(1))
                                .passivoitu(true).tehtavanimike("Opettaja")
                                .organisaatio(OrganisaatioDto.builder().oid("1.2.3.4.2").build()).build()
                ));

        List<OrganisaatioHenkiloWithOrganisaatioDto> result = organisaatioHenkiloService.listOrganisaatioHenkilos("1.2.3.4.5", "fi");
        assertEquals(2, result.size());
        assertEquals("1.2.3.4.2", result.get(0).getOrganisaatio().getOid()); // O < S
        assertEquals(2L, result.get(0).getId());
        assertEquals("Only in English", result.get(0).getOrganisaatio().getNimi().getOrAny("fi").orElse(null));
        assertEquals(true, result.get(0).isPassivoitu());
        assertEquals("1.2.3.4.1", result.get(1).getOrganisaatio().getOid());
        assertEquals("Suomeksi", result.get(1).getOrganisaatio().getNimi().get("fi"));
        assertEquals(asList("Tyyppi1", "Tyyppi2"), result.get(1).getOrganisaatio().getTyypit());
        assertEquals(LocalDate.now(), result.get(1).getVoimassaAlkuPvm());
        assertEquals(LocalDate.now().plusYears(1), result.get(1).getVoimassaLoppuPvm());
        assertEquals("Devaaja", result.get(1).getTehtavanimike());
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void listOrganisaatioPerustiedotForCurrentUserTest() throws Exception {
        given(this.organisaatioHenkiloRepository.findDistinctOrganisaatiosForHenkiloOid("1.2.3.4.5"))
                .willReturn(singletonList("2.3.4.5.6"));
        given(this.organisaatioClient.listActiveOrganisaatioPerustiedotByOidRestrictionList(singletonList("2.3.4.5.6")))
                .willReturn(singletonList(readJson(jsonResource("classpath:organisaatio/organisaatioPerustiedot.json"), OrganisaatioPerustieto.class)));

        List<OrganisaatioPerustieto> result = organisaatioHenkiloService.listOrganisaatioPerustiedotForCurrentUser();
        assertEquals(1, result.size());
        assertEquals("1.2.246.562.10.14175756379", result.get(0).getOid());
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void listPossibleHenkiloTypesAccessibleForCurrentUserRekisterinpitajaTest() {
        given(this.kayttoOikeusRepository
                .isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.5", PALVELU_HENKILONHALLINTA, ROLE_ADMIN))
                .willReturn(true);

        List<HenkiloTyyppi> list = organisaatioHenkiloService.listPossibleHenkiloTypesAccessibleForCurrentUser();
        assertEquals(new HashSet<>(asList(HenkiloTyyppi.VIRKAILIJA, HenkiloTyyppi.PALVELU)), new HashSet<>(list));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void listPossibleHenkiloTypesAccessibleForCurrentUserCrudTest() {
        given(this.kayttoOikeusRepository
                .isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.5", PALVELU_HENKILONHALLINTA, ROLE_ADMIN))
                .willReturn(false);
        given(this.kayttoOikeusRepository
                .isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.5", PALVELU_HENKILONHALLINTA, ROLE_CRUD))
                .willReturn(true);

        List<HenkiloTyyppi> list = organisaatioHenkiloService.listPossibleHenkiloTypesAccessibleForCurrentUser();
        assertEquals(new HashSet<>(asList(HenkiloTyyppi.VIRKAILIJA)), new HashSet<>(list));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void findOrganisaatioHenkiloByHenkiloAndOrganisaatioTest() {
        given(this.organisaatioHenkiloRepository.findByHenkiloOidAndOrganisaatioOid("1.2.3.4.5", "5.6.7.8.9"))
                .willReturn(Optional.of(OrganisaatioHenkiloDto.builder()
                        .id(33L).organisaatioOid("5.6.7.8.9").build()));

        OrganisaatioHenkiloDto organisaatioHenkilo = organisaatioHenkiloService.findOrganisaatioHenkiloByHenkiloAndOrganisaatio("1.2.3.4.5", "5.6.7.8.9");
        assertNotNull(organisaatioHenkilo);
        assertEquals(33L, organisaatioHenkilo.getId());
        assertEquals("5.6.7.8.9", organisaatioHenkilo.getOrganisaatioOid());
    }

    @Test(expected = NotFoundException.class)
    @WithMockUser(username = "1.2.3.4.5")
    public void findOrganisaatioHenkiloByHenkiloAndOrganisaatioErrorTest() {
        given(this.organisaatioHenkiloRepository.findByHenkiloOidAndOrganisaatioOid("1.2.3.4.5", "1.1.1.1.1")).willReturn(Optional.empty());
        organisaatioHenkiloService.findOrganisaatioHenkiloByHenkiloAndOrganisaatio("1.2.3.4.5", "1.1.1.1.1");
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void passivoiHenkiloOrganisation() {
        OrganisaatioHenkilo organisaatioHenkilo = populate(organisaatioHenkilo(henkilo("henkilo1"), "1.1.1.1.1"));
        populate(henkilo("1.2.3.4.5"));
        given(this.organisaatioHenkiloRepository.findByHenkiloOidHenkiloAndOrganisaatioOid("1.2.3.4.5", "1.1.1.1.1"))
                .willReturn(Optional.of(organisaatioHenkilo));
        KayttoOikeusRyhma kayttoOikeusRyhma = populate(kayttoOikeusRyhma("käyttöoikeusryhmä"));
        MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma = populate(kayttooikeusTapahtuma(organisaatioHenkilo, kayttoOikeusRyhma));

        this.organisaatioHenkiloService.passivoiHenkiloOrganisation("1.2.3.4.5", "1.1.1.1.1");
        assertThat(organisaatioHenkilo.isPassivoitu()).isTrue();
        assertThat(organisaatioHenkilo.getMyonnettyKayttoOikeusRyhmas()).isEmpty();
        assertThat(organisaatioHenkilo.getKayttoOikeusRyhmaHistorias()).extracting("tila").containsExactly(SULJETTU);
    }

    @Test(expected = NotFoundException.class)
    @WithMockUser(username = "1.2.3.4.5")
    public void passivoiHenkiloOrganisationNotFound() {
        given(this.organisaatioHenkiloRepository.findByHenkiloOidHenkiloAndOrganisaatioOid("1.2.3.4.5", "1.1.1.1.1"))
                .willReturn(Optional.empty());
        organisaatioHenkiloService.passivoiHenkiloOrganisation("1.2.3.4.5", "1.1.1.1.1");
    }
}
