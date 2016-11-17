package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloDto;
import fi.vm.sade.kayttooikeus.model.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloRepository;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static fi.vm.sade.kayttooikeus.util.JsonUtil.readJson;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class OrganisaatioHenkiloServiceTest extends AbstractServiceTest {
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
    public void listOrganisaatioPerustiedotForCurrentUserTest() {
        given(this.organisaatioHenkiloRepository.findDistinctOrganisaatiosForHenkiloOid("1.2.3.4.5"))
                .willReturn(singletonList("2.3.4.5.6"));
        given(this.organisaatioClient.listOganisaatioPerustiedot(singletonList("2.3.4.5.6")))
                .willReturn(singletonList(readJson(jsonResource("classpath:organisaatio/organisaatioPerustiedot.json"), OrganisaatioPerustieto.class)));

        List<OrganisaatioPerustieto> result = organisaatioHenkiloService.listOrganisaatioPerustiedotForCurrentUser();
        assertEquals(1, result.size());
        assertEquals("1.2.246.562.10.14175756379", result.get(0).getOid());
    }
    
    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void listPossibleHenkiloTypesAccessibleForCurrentUserRekisterinpitajaTest() {
        given(this.kayttoOikeusRepository.isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.5", "HENKILONHALLINTA", "OPHREKISTERI")).willReturn(true);

        List<HenkiloTyyppi> list = organisaatioHenkiloService.listPossibleHenkiloTypesAccessibleForCurrentUser();
        assertEquals(new HashSet<>(asList(HenkiloTyyppi.VIRKAILIJA, HenkiloTyyppi.PALVELU)), new HashSet<>(list));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void listPossibleHenkiloTypesAccessibleForCurrentUserCrudTest() {
        given(this.kayttoOikeusRepository.isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.5", "HENKILONHALLINTA", "OPHREKISTERI")).willReturn(false);
        given(this.kayttoOikeusRepository.isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.5", "HENKILONHALLINTA", "CRUD")).willReturn(true);

        List<HenkiloTyyppi> list = organisaatioHenkiloService.listPossibleHenkiloTypesAccessibleForCurrentUser();
        assertEquals(new HashSet<>(asList(HenkiloTyyppi.VIRKAILIJA)), new HashSet<>(list));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void findOrganisaatioHenkiloByHenkiloAndOrganisaatioTest() {
        given(this.organisaatioHenkiloRepository.findByHenkiloOidAndOrganisaatioOid("1.2.3.4.5", "5.6.7.8.9"))
                .willReturn(Optional.of(OrganisaatioHenkiloDto.builder()
                        .id(33L)
                        .organisaatioOid("5.6.7.8.9")
                        .build()));

        OrganisaatioHenkiloDto organisaatioHenkilo = organisaatioHenkiloService.findOrganisaatioHenkiloByHenkiloAndOrganisaatio("1.2.3.4.5", "5.6.7.8.9");
        assertNotNull(organisaatioHenkilo);
        assertEquals(Long.valueOf(33), organisaatioHenkilo.getId());
        assertEquals("5.6.7.8.9", organisaatioHenkilo.getOrganisaatioOid());
    }

    @Test(expected = NotFoundException.class)
    @WithMockUser(username = "1.2.3.4.5")
    public void findOrganisaatioHenkiloByHenkiloAndOrganisaatioErrorTest() {
        given(this.organisaatioHenkiloRepository.findByHenkiloOidAndOrganisaatioOid("1.2.3.4.5", "1.1.1.1.1")).willReturn(Optional.empty());
        organisaatioHenkiloService.findOrganisaatioHenkiloByHenkiloAndOrganisaatio("1.2.3.4.5", "1.1.1.1.1");
    }

}
