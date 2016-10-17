package fi.vm.sade.kayttooikeus.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

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
}
