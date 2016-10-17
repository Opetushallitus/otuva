package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusPopulator.oikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloKayttoOikeusPopulator.myonnettyKayttoOikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class KayttoOikeusRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private KayttoOikeusRepository kayttoOikeusRepository;
    
    @Test
    public void isHenkiloMyonnettyKayttoOikeusToPalveluInRoleTest() {
        MyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma = populate(myonnettyKayttoOikeus(
            organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
            kayttoOikeusRyhma("RYHMA")
                    .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                    .withOikeus(oikeus("KOODISTO", "READ"))
        ));
        
        assertTrue(kayttoOikeusRepository.isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.5", "HENKILOHALLINTA", "CRUD"));
        assertTrue(kayttoOikeusRepository.isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.5", "KOODISTO", "READ"));
        assertFalse(kayttoOikeusRepository.isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.5", "KOODISTO", "CRUD"));
        assertFalse(kayttoOikeusRepository.isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.5", "PALVELU", "UPDATE"));
        assertFalse(kayttoOikeusRepository.isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.6", "HENKILOHALLINTA", "CRUD"));
        
        tapahtuma.setVoimassaAlkuPvm(new LocalDate().plusDays(1));
        em.merge(tapahtuma);

        assertFalse(kayttoOikeusRepository.isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.5", "HENKILOHALLINTA", "CRUD"));
        
        populate(myonnettyKayttoOikeus(
            organisaatioHenkilo(henkilo("1.2.3.4.6"), "3.4.5.6.7").voimassaAlkaen(new LocalDate().plusMonths(1)),
            kayttoOikeusRyhma("RYHMA").withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
        ));
        assertFalse(kayttoOikeusRepository.isHenkiloMyonnettyKayttoOikeusToPalveluInRole("1.2.3.4.6", "HENKILOHALLINTA", "CRUD"));
    }

}
