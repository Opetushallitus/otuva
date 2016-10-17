package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusPopulator.oikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class KayttoOikeusRyhmaRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository;

    @Test
    public void listAllTest() {
        populate(kayttoOikeusRyhma("RYHMÄ")
                .withOikeus(oikeus("APP1", "READ")) // ensure unique ryhmä
                .withOikeus(oikeus("APP2", "WRITE")));
        
        List<KayttoOikeusRyhma> ryhmas = kayttoOikeusRyhmaRepository.listAll();
        assertEquals(1, ryhmas.size());
        assertEquals("RYHMÄ", ryhmas.get(0).getName());
    }
}
