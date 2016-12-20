package fi.vm.sade.kayttooikeus.repositories;

import com.google.common.collect.Lists;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloWithOrganisaatioDto;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloTyyppi.OPISKELIJA;
import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static org.junit.Assert.*;

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

        List<OrganisaatioHenkiloWithOrganisaatioDto> results = organisaatioHenkiloRepository.findActiveOrganisaatioHenkiloListDtos("1.2.3.4.5");
        assertEquals(2, results.size());
        assertEquals("3.4.5.6.7", results.get(0).getOrganisaatio().getOid());
        assertEquals(oh1.getId(), Long.valueOf(results.get(0).getId()));
        assertEquals("3.4.5.6.8", results.get(1).getOrganisaatio().getOid());
        assertEquals(oh2.getId(), Long.valueOf(results.get(1).getId()));
        assertEquals(oh2.getTehtavanimike(), results.get(1).getTehtavanimike());
        assertEquals(OPISKELIJA, results.get(1).getOrganisaatioHenkiloTyyppi());
        assertEquals(oh2.getVoimassaAlkuPvm(), results.get(1).getVoimassaAlkuPvm());
        assertEquals(oh2.getVoimassaLoppuPvm(), results.get(1).getVoimassaLoppuPvm());
    }

    @Test
    public void findByHenkiloOidAndOrganisaatioOidTest() {
        populate(organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"));
        Optional<OrganisaatioHenkiloDto> organisaatioHenkilo = organisaatioHenkiloRepository.findByHenkiloOidAndOrganisaatioOid("1.2.3.4.5", "3.4.5.6.7");
        assertTrue(organisaatioHenkilo.isPresent());
        assertEquals("3.4.5.6.7", organisaatioHenkilo.get().getOrganisaatioOid());

        organisaatioHenkilo = organisaatioHenkiloRepository.findByHenkiloOidAndOrganisaatioOid("1.2.3.4.5", "1.1.1.1.madeup");
        assertFalse(organisaatioHenkilo.isPresent());

        organisaatioHenkilo = organisaatioHenkiloRepository.findByHenkiloOidAndOrganisaatioOid("1.2.3.4.madeup", "3.4.5.6.7");
        assertFalse(organisaatioHenkilo.isPresent());
    }

    @Test
    public void findOrganisaatioHenkilosForHenkiloTest() throws Exception {
        populate(organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"));
        populate(organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.8"));
        populate(organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.9"));
        populate(organisaatioHenkilo(henkilo("1.2.3.4.6"), "3.4.5.6.9"));

        List<OrganisaatioHenkiloDto> organisaatioHenkilos = organisaatioHenkiloRepository.findOrganisaatioHenkilosForHenkilo("1.2.3.4.5");
        assertEquals(3, organisaatioHenkilos.size());
        List<String> oids = organisaatioHenkilos.stream().map(OrganisaatioHenkiloDto::getOrganisaatioOid).collect(Collectors.toList());
        assertTrue(oids.containsAll(Lists.newArrayList("3.4.5.6.7", "3.4.5.6.8", "3.4.5.6.9")));
    }
}
