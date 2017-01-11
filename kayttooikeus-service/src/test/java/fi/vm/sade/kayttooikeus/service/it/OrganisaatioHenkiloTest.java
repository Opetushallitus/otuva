package fi.vm.sade.kayttooikeus.service.it;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloCreateDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloDto;
import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioCachePopulator.organisaatioCache;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloUpdateDto;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class OrganisaatioHenkiloTest extends AbstractServiceIntegrationTest {
    @MockBean
    private OrganisaatioClient organisaatioClient;

    @MockBean
    private PermissionCheckerService permissionCheckerService;

    @Autowired
    private OrganisaatioHenkiloService organisaatioHenkiloService;

    @Test
    public void addOrganisaatioHenkilotShouldOnlyAddNewOrganisaatio() {
        populate(organisaatioCache("organisaatio1"));
        populate(organisaatioCache("organisaatio2"));
        populate(organisaatioCache("organisaatio3"));
        populate(organisaatioHenkilo(henkilo("henkilo1"), "organisaatio1").tehtavanimike("tehtävä1"));
        populate(organisaatioHenkilo(henkilo("henkilo1"), "organisaatio3").tehtavanimike("tehtävä3"));
        List<OrganisaatioHenkiloCreateDto> organisaatioHenkilot = new ArrayList<>();
        OrganisaatioHenkiloCreateDto organisaatio1 = new OrganisaatioHenkiloCreateDto();
        organisaatio1.setOrganisaatioOid("organisaatio1");
        organisaatio1.setTehtavanimike("tehtävä1-päivitetty");
        organisaatioHenkilot.add(organisaatio1);
        OrganisaatioHenkiloCreateDto organisaatio2 = new OrganisaatioHenkiloCreateDto();
        organisaatio2.setOrganisaatioOid("organisaatio2");
        organisaatio2.setTehtavanimike("tehtävä2");
        organisaatioHenkilot.add(organisaatio2);

        List<OrganisaatioHenkiloDto> addOrganisaatioHenkilot = organisaatioHenkiloService.addOrganisaatioHenkilot("henkilo1", organisaatioHenkilot);

        assertThat(addOrganisaatioHenkilot).extracting("organisaatioOid", "tehtavanimike")
                .containsExactlyInAnyOrder(
                        tuple("organisaatio1", "tehtävä1"),
                        tuple("organisaatio2", "tehtävä2"),
                        tuple("organisaatio3", "tehtävä3")
                );
    }

    @Test
    @WithMockUser("henkilo1")
    public void CreateOrUpdateOrganisaatioHenkilos() {
        populate(organisaatioCache("organisaatio1"));
        populate(organisaatioCache("organisaatio2"));
        populate(organisaatioCache("organisaatio3"));
        populate(organisaatioHenkilo(henkilo("henkilo1"), "organisaatio1").tehtavanimike("tehtävä1"));
        populate(organisaatioHenkilo(henkilo("henkilo1"), "organisaatio3").tehtavanimike("tehtävä3"));

        List<OrganisaatioHenkiloUpdateDto> organisaatioHenkilot = new ArrayList<>();
        OrganisaatioHenkiloUpdateDto organisaatio1 = new OrganisaatioHenkiloUpdateDto();
        organisaatio1.setOrganisaatioOid("organisaatio1");
        organisaatio1.setTehtavanimike("tehtävä1-päivitetty");
        organisaatioHenkilot.add(organisaatio1);
        OrganisaatioHenkiloUpdateDto organisaatio2 = new OrganisaatioHenkiloUpdateDto();
        organisaatio2.setOrganisaatioOid("organisaatio2");
        organisaatio2.setTehtavanimike("tehtävä2");
        organisaatioHenkilot.add(organisaatio2);

        List<OrganisaatioHenkiloDto> addOrganisaatioHenkilot = this.organisaatioHenkiloService.createOrUpdateOrganisaatioHenkilos("henkilo1", organisaatioHenkilot);

        assertThat(addOrganisaatioHenkilot).extracting("organisaatioOid", "tehtavanimike")
                .containsExactlyInAnyOrder(
                        tuple("organisaatio1", "tehtävä1-päivitetty"),
                        tuple("organisaatio2", "tehtävä2"),
                        tuple("organisaatio3", "tehtävä3")
                );
    }
}
