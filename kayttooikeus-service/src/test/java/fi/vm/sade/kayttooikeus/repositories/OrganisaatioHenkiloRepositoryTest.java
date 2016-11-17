package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloListDto;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloTyyppi.OPISKELIJA;
import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class OrganisaatioHenkiloRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private OrganisaatioHenkiloRepository organisaatioHenkiloRepository;
    
    @Test
    public void findDistinctOrganisaatiosForHenkiloOidEmptyTest() {
        List<String> results = organisaatioHenkiloRepository.findDistinctOrganisaatiosForHenkiloOid("oid");
        assertEquals(0, results.size());
    }
    
    @Test
    public void findDistinctOrganisaatiosForHenkiloOidTest() {
        populate(organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"));
        List<String> results = organisaatioHenkiloRepository.findDistinctOrganisaatiosForHenkiloOid("1.2.3.4.5");
        assertEquals(1, results.size());
        assertEquals("3.4.5.6.7", results.get(0));
    }

    @Test
    public void findOrganisaatioHenkiloListDtosTest() {
        OrganisaatioHenkilo oh1 = populate(organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"));
        OrganisaatioHenkilo oh2 =populate(organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.8")
                .voimassaAlkaen(new LocalDate().minusDays(2)).tyyppi(OPISKELIJA)
                .voimassaAsti(new LocalDate().plusYears(1))
                .tehtavanimike("Devaaja"));
        populate(organisaatioHenkilo(henkilo("1.2.3.4.6"), "3.4.5.6.9"));

        List<OrganisaatioHenkiloListDto> results = organisaatioHenkiloRepository.findOrganisaatioHenkiloListDtos("1.2.3.4.5");
        assertEquals(2, results.size());
        assertEquals("3.4.5.6.7", results.get(0).getOrganisaatio().getOid());
        assertEquals(oh1.getId(), Long.valueOf(results.get(0).getId()));
        assertEquals("3.4.5.6.8", results.get(1).getOrganisaatio().getOid());
        assertEquals(oh2.getId(), Long.valueOf(results.get(1).getId()));
        assertEquals(oh2.getTehtavanimike(), results.get(1).getTehtavanimike());
        assertEquals(OPISKELIJA, results.get(1).getTyyppi());
        assertEquals(oh2.getVoimassaAlkuPvm(), results.get(1).getVoimassaAlkuPvm());
        assertEquals(oh2.getVoimassaLoppuPvm(), results.get(1).getVoimassaLoppuPvm());
    }
}
