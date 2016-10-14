package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.config.ApplicationTest;
import fi.vm.sade.kayttooikeus.model.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusDao;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloDao;
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

import static fi.vm.sade.kayttooikeus.util.JsonUtil.readJson;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

/**
 * User: tommiratamaa
 * Date: 14/10/2016
 * Time: 13.34
 */
@RunWith(SpringRunner.class)
@ApplicationTest
public class OrganisaatioHenkiloServiceTest extends AbstractServiceTest {

    @MockBean
    private OrganisaatioClient organisaatioClient;

    @MockBean
    private OrganisaatioHenkiloDao organisaatioHenkiloDao;

    @MockBean
    private KayttoOikeusDao kayttoOikeusDao;
    
    @Autowired
    private OrganisaatioHenkiloService organisaatioHenkiloService;
    
    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void listOrganisaatioPerustiedotForCurrentUserTest() {
        given(this.organisaatioHenkiloDao.findDistinctOrganisaatiosForHenkiloOid("1.2.3.4.5"))
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
        given(this.kayttoOikeusDao.isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.5", "HENKILONHALLINTA", "OPHREKISTERI")).willReturn(true);

        List<HenkiloTyyppi> list = organisaatioHenkiloService.listPossibleHenkiloTypesAccessibleForCurrentUser();
        assertEquals(new HashSet<>(asList(HenkiloTyyppi.VIRKAILIJA, HenkiloTyyppi.PALVELU)), new HashSet<>(list));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    public void listPossibleHenkiloTypesAccessibleForCurrentUserCrudTest() {
        given(this.kayttoOikeusDao.isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.5", "HENKILONHALLINTA", "OPHREKISTERI")).willReturn(false);
        given(this.kayttoOikeusDao.isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.5", "HENKILONHALLINTA", "CRUD")).willReturn(true);

        List<HenkiloTyyppi> list = organisaatioHenkiloService.listPossibleHenkiloTypesAccessibleForCurrentUser();
        assertEquals(new HashSet<>(asList(HenkiloTyyppi.VIRKAILIJA)), new HashSet<>(list));
    }
}
