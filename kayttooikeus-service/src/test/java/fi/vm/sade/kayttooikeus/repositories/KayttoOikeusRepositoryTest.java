package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeusHistoriaDto;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeusTyyppi;
import fi.vm.sade.kayttooikeus.dto.PalveluKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import org.joda.time.LocalDate;
import org.joda.time.Period;
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
import static fi.vm.sade.kayttooikeus.repositories.populate.TextGroupPopulator.text;
import static org.junit.Assert.*;

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

    @Test
    public void findSoonToBeExpiredTapahtumasTest() {
        MyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA").withKuvaus(text("FI", "Kuvaus"))
                        .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                        .withOikeus(oikeus("KOODISTO", "READ"))
        ).voimassaPaattyen(new LocalDate().plusMonths(3)));

        List<ExpiringKayttoOikeusDto> expiring = kayttoOikeusRepository.findSoonToBeExpiredTapahtumas(new LocalDate(), Period.weeks(1));
        assertTrue(expiring.isEmpty());

        expiring = kayttoOikeusRepository.findSoonToBeExpiredTapahtumas(new LocalDate());
        assertTrue(expiring.isEmpty());

        expiring = kayttoOikeusRepository.findSoonToBeExpiredTapahtumas(new LocalDate(), Period.months(4), Period.months(2));
        assertTrue(expiring.isEmpty());
        
        expiring = kayttoOikeusRepository.findSoonToBeExpiredTapahtumas(new LocalDate(), Period.months(3), Period.months(1));
        assertFalse(expiring.isEmpty());
        assertEquals(1, expiring.size());
        assertEquals(tapahtuma.getVoimassaLoppuPvm(), expiring.get(0).getVoimassaLoppuPvm());
        assertEquals(tapahtuma.getId(), expiring.get(0).getMyonnettyTapahtumaId());
        assertEquals("1.2.3.4.5", expiring.get(0).getHenkiloOid());
        assertEquals("RYHMA", expiring.get(0).getRyhmaName());
        assertEquals(tapahtuma.getKayttoOikeusRyhma().getDescription().getId(), expiring.get(0).getRyhmaDescription().getId());
    }

    @Test
    public void listKayttoOikeusByPalveluTest() {
        populate(kayttoOikeusRyhma("RYHMA").withKuvaus(text("FI", "Kuvaus"))
                        .withOikeus(oikeus("HENKILOHALLINTA", "CRUD").kuvaus(text("FI", "Kuvaus")
                                .put("EN", "Desc")))
                        .withOikeus(oikeus("HENKILOHALLINTA", "READ"))
                        .withOikeus(oikeus("KOODISTO", "READ")));
        List<PalveluKayttoOikeusDto> results = kayttoOikeusRepository.listKayttoOikeusByPalvelu("PALVELU2");
        assertTrue(results.isEmpty());

        results = kayttoOikeusRepository.listKayttoOikeusByPalvelu("HENKILOHALLINTA");
        assertEquals(2, results.size());
        assertEquals("CRUD", results.get(0).getRooli());
        assertEquals("READ", results.get(1).getRooli());
        assertNotNull(results.get(0).getOikeusLangs());
    }

    @Test
    public void listMyonnettyKayttoOikeusHistoriaForHenkiloTest() {
        MyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma1 = populate(myonnettyKayttoOikeus(
                    organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                    kayttoOikeusRyhma("RYHMA").withKuvaus(text("FI", "Kuvaus"))
                            .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                            .withOikeus(oikeus("KOODISTO", "READ")))
                        .voimassaPaattyen(new LocalDate())),
            tapahtuma2 = populate(myonnettyKayttoOikeus(
                    organisaatioHenkilo(henkilo("1.2.3.4.5"), "4.5.6.7.8").tehtavanimike("testaaja"),
                    kayttoOikeusRyhma("RYHMA2")
                            .withOikeus(oikeus("KOODISTO", "WRITE")))
                    .voimassaPaattyen(new LocalDate().plusMonths(3)));

        List<KayttoOikeusHistoriaDto> historia = kayttoOikeusRepository.listMyonnettyKayttoOikeusHistoriaForHenkilo("1.2.3.4.5");
        assertEquals(3, historia.size());
        assertEquals(tapahtuma2.getAikaleima(), historia.get(0).getAikaleima());
        assertEquals(tapahtuma2.getKayttoOikeusRyhma().getKayttoOikeus().iterator().next().getId().longValue(),
                historia.get(0).getKayttoOikeusId());
        assertEquals(tapahtuma2.getTila(), historia.get(0).getTila());
        assertEquals(KayttoOikeusTyyppi.KOOSTEROOLI, historia.get(0).getTyyppi());
        assertEquals("4.5.6.7.8", historia.get(0).getOrganisaatioOid());
        assertEquals("testaaja", historia.get(0).getTehtavanimike());
        assertEquals(new LocalDate().plusMonths(3), historia.get(0).getVoimassaLoppuPvm());
        assertEquals(new LocalDate(), historia.get(0).getVoimassaAlkuPvm());
        assertEquals(tapahtuma2.getKasittelija().getOidHenkilo(), historia.get(0).getKasittelija());
        assertEquals(tapahtuma2.getKayttoOikeusRyhma().getDescription().getId(), historia.get(0).getKuvaus().getId());
    }
}
