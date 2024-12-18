package fi.vm.sade.kayttooikeus.service.it;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloTyyppi;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaCriteriaDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaReadDto;
import fi.vm.sade.kayttooikeus.service.PalvelukayttajaService;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;

import java.util.List;

import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.palvelukayttaja;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class PalvelukayttajaServiceTest extends AbstractServiceIntegrationTest {
    @Autowired
    private PalvelukayttajaService palvelukayttajaService;
    @MockBean
    private OrganisaatioClient organisaatioClient;

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void listWithCriteria() throws Exception {
        when(this.organisaatioClient.getChildOids(eq("1.2.3.4.200")))
                .thenReturn(List.of("1.2.3.4.600"));

        populate(palvelukayttaja("1.2.3.4.500")
                .withNimet("_", "one")
                .withUsername("one"));
        populate(organisaatioHenkilo("1.2.3.4.500", "1.2.3.4.600")
                .tyyppi(OrganisaatioHenkiloTyyppi.PALVELU));

        populate(palvelukayttaja("1.2.3.4.100")
                .withNimet("_", "two")
                .withUsername("two"));
        populate(organisaatioHenkilo("1.2.3.4.100", "1.2.3.4.200")
                .tyyppi(OrganisaatioHenkiloTyyppi.PALVELU));

        var requestAll = new PalvelukayttajaCriteriaDto();
        List<PalvelukayttajaReadDto> responseAll = palvelukayttajaService.list(requestAll);
        assertThat(responseAll.size()).isEqualTo(2);

        var requestByName = new PalvelukayttajaCriteriaDto();
        requestByName.setNameQuery("two");
        List<PalvelukayttajaReadDto> responseByName = palvelukayttajaService.list(requestByName);
        assertThat(responseByName.size()).isEqualTo(1);
        assertThat(responseByName.get(0).getKayttajatunnus()).isEqualTo("two");

        var requestByOrganisaatioOid = new PalvelukayttajaCriteriaDto();
        requestByOrganisaatioOid.setOrganisaatioOid("1.2.3.4.200");
        List<PalvelukayttajaReadDto> responseByOrganisaatioOid = palvelukayttajaService.list(requestByOrganisaatioOid);
        assertThat(responseByOrganisaatioOid.size()).isEqualTo(1);
        assertThat(responseByOrganisaatioOid.get(0).getKayttajatunnus()).isEqualTo("two");

        var requestBySubOrganisations = new PalvelukayttajaCriteriaDto();
        requestBySubOrganisations.setOrganisaatioOid("1.2.3.4.200");
        requestBySubOrganisations.setSubOrganisation(true);
        List<PalvelukayttajaReadDto> responseBySubOrganisations = palvelukayttajaService.list(requestBySubOrganisations);
        assertThat(responseBySubOrganisations.size()).isEqualTo(2);
    }
}
