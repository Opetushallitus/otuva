package fi.vm.sade.kayttooikeus.repositories;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusPopulator.oikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloKayttoOikeusPopulator.myonnettyKayttoOikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class OrganisaatioHenkiloDaoTest extends AbstractRepositoryTest {
    @Autowired
    private OrganisaatioHenkiloDao organisaatioHenkiloDao;
    
    @Test
    public void findDistinctOrganisaatiosForHenkiloOidEmptyTest() {
        List<String> results = organisaatioHenkiloDao.findDistinctOrganisaatiosForHenkiloOid("oid");
        assertEquals(0, results.size());
    }
    
    @Test //TODO:
    @Ignore // for some reason FK_ON2C9008M1EYWND7BO0L8I5VL table: ORGANISAATIOHENKILO
    public void findDistinctOrganisaatiosForHenkiloOidTest() {
        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA")
                    .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                    .withOikeus(oikeus("KOODISTO", "READ"))
            ));
        List<String> results = organisaatioHenkiloDao.findDistinctOrganisaatiosForHenkiloOid("oid");
        assertEquals(2, results.size());
    }
}
