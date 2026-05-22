package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.PalveluKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.dto.PalveluRooliDto;
import fi.vm.sade.kayttooikeus.model.KayttoOikeus;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;

import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusPopulator.oikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloKayttoOikeusPopulator.myonnettyKayttoOikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.TextGroupPopulator.text;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class KayttoOikeusRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private KayttoOikeusRepository kayttoOikeusRepository;

    @Test
    public void findSoonToBeExpiredTapahtumasTest() {
        MyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA").withNimi(text("FI", "Kuvaus"))
                        .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                        .withOikeus(oikeus("KOODISTO", "READ"))
        ).voimassaPaattyen(LocalDate.now().plusMonths(3)));

        List<ExpiringKayttoOikeusDto> expiring = kayttoOikeusRepository.findSoonToBeExpiredTapahtumas(LocalDate.now(), Period.ofWeeks(1));
        assertTrue(expiring.isEmpty());

        expiring = kayttoOikeusRepository.findSoonToBeExpiredTapahtumas(LocalDate.now());
        assertTrue(expiring.isEmpty());

        expiring = kayttoOikeusRepository.findSoonToBeExpiredTapahtumas(LocalDate.now(), Period.ofMonths(4), Period.ofMonths(2));
        assertTrue(expiring.isEmpty());

        expiring = kayttoOikeusRepository.findSoonToBeExpiredTapahtumas(LocalDate.now(), Period.ofMonths(3), Period.ofMonths(1));
        assertFalse(expiring.isEmpty());
        assertEquals(1, expiring.size());
        assertEquals(tapahtuma.getVoimassaLoppuPvm(), expiring.get(0).getVoimassaLoppuPvm());
        assertEquals(tapahtuma.getId(), expiring.get(0).getMyonnettyTapahtumaId());
        assertEquals("1.2.3.4.5", expiring.get(0).getHenkiloOid());
        assertEquals("RYHMA", expiring.get(0).getRyhmaName());
        assertEquals(tapahtuma.getKayttoOikeusRyhma().getNimi().getId(), expiring.get(0).getRyhmaDescription().getId());
    }

    @Test
    public void listKayttoOikeusByPalveluTest() {
        populate(kayttoOikeusRyhma("RYHMA").withNimi(text("FI", "Kuvaus"))
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
    public void findPalveluRoolitByKayttoOikeusRyhmaIdTest() throws Exception {
        KayttoOikeusRyhma kayttoOikeusRyhma = populate(kayttoOikeusRyhma("RYHMA")
                .withOikeus(oikeus("HENKILOHALLINTA", "CRUD")
                        .kuvaus(text("FI", "Kuvaus").put("SV", "på svenska").put("EN", "desc")))
                .withOikeus(oikeus("KOODISTO", "READ")
                        .kuvaus(text("FI", "Kuvaus henkilöhallinta").put("SV", "på svenska").put("EN", "desc"))));

        List<PalveluRooliDto> palveluRoolis = kayttoOikeusRepository.findPalveluRoolitByKayttoOikeusRyhmaId(kayttoOikeusRyhma.getId());
        assertEquals(2, palveluRoolis.size());

        assertTrue(palveluRoolis.stream()
                .map(PalveluRooliDto::getPalveluName)
                .collect(toList())
                .containsAll(Arrays.asList("HENKILOHALLINTA", "KOODISTO")));
    }

    @Test
    public void findByRooliAndPalveluTest() throws Exception {
        KayttoOikeus kayttoOikeus = kayttoOikeusRepository.findByRooliAndPalvelu("rooli", "palvelu");
        assertNull(kayttoOikeus);

        populate(kayttoOikeusRyhma("RYHMA")
                .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                .withOikeus(oikeus("HENKILOHALLINTA", "READ"))
                .withOikeus(oikeus("KOODISTO", "READ")));

        kayttoOikeus = kayttoOikeusRepository.findByRooliAndPalvelu("READ", "KOODISTO");
        assertNotNull(kayttoOikeus);
        assertEquals("READ", kayttoOikeus.getRooli());
        assertEquals("KOODISTO", kayttoOikeus.getPalvelu().getName());

        kayttoOikeus = kayttoOikeusRepository.findByRooliAndPalvelu("READ", "HENKILOHALLINTA");
        assertNotNull(kayttoOikeus);
        assertEquals("READ", kayttoOikeus.getRooli());
        assertEquals("HENKILOHALLINTA", kayttoOikeus.getPalvelu().getName());

        kayttoOikeus = kayttoOikeusRepository.findByRooliAndPalvelu("CRUD", "HENKILOHALLINTA");
        assertNotNull(kayttoOikeus);
        assertEquals("CRUD", kayttoOikeus.getRooli());
        assertEquals("HENKILOHALLINTA", kayttoOikeus.getPalvelu().getName());

    }

}
