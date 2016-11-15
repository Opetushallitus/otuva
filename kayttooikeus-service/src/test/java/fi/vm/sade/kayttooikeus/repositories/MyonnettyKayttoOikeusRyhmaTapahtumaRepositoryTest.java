package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.dto.MyonnettyKayttoOikeusDto;
import org.joda.time.LocalDate;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class MyonnettyKayttoOikeusRyhmaTapahtumaRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository;

    @Test
    public void findMasterIdsByHenkiloTest(){
        Long id = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA").withKuvaus(text("FI", "Kuvaus"))
                        .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                        .withOikeus(oikeus("KOODISTO", "READ")))
                .voimassaPaattyen(new LocalDate())).getKayttoOikeusRyhma().getId();

        Long id2 = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "4.5.6.7.8").tehtavanimike("testaaja"),
                kayttoOikeusRyhma("RYHMA2")
                        .withOikeus(oikeus("KOODISTO", "WRITE")))
                .voimassaPaattyen(new LocalDate().plusMonths(3))).getKayttoOikeusRyhma().getId();

        Long id3 = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.7"), "4.5.6.7.8").tehtavanimike("testaaja"),
                kayttoOikeusRyhma("RYHMA2")
                        .withOikeus(oikeus("KOODISTO", "WRITE")))
                .voimassaPaattyen(new LocalDate().plusMonths(3))).getKayttoOikeusRyhma().getId();

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.8")
                        .withPassivoitu(true), "4.5.6.7.8")
                        .tehtavanimike("testaaja"),
                kayttoOikeusRyhma("RYHMA2")
                        .withOikeus(oikeus("KOODISTO", "WRITE")))
                .voimassaPaattyen(new LocalDate().plusMonths(3)));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.9"), "4.5.6.7.8")
                        .tehtavanimike("testaaja"),
                kayttoOikeusRyhma("RYHMA2")
                        .withOikeus(oikeus("KOODISTO", "WRITE")))
                .voimassaAlkaen(new LocalDate().minusMonths(2))
                .voimassaPaattyen(new LocalDate().minusMonths(1)));

        populate(organisaatioHenkilo(henkilo("123.123.123.123"), "111.111.111.111"));
        List<Long> tapahtumas = myonnettyKayttoOikeusRyhmaTapahtumaRepository.findMasterIdsByHenkilo("1.2.3.4.5");
        assertEquals(2, tapahtumas.size());
        assertTrue(tapahtumas.contains(id));
        assertTrue(tapahtumas.contains(id2));

        tapahtumas = myonnettyKayttoOikeusRyhmaTapahtumaRepository.findMasterIdsByHenkilo("1.2.3.4.7");
        assertEquals(1, tapahtumas.size());
        assertEquals(id3, tapahtumas.get(0));

        tapahtumas = myonnettyKayttoOikeusRyhmaTapahtumaRepository.findMasterIdsByHenkilo("1.2.3.4.8");
        assertEquals(0, tapahtumas.size());

        tapahtumas = myonnettyKayttoOikeusRyhmaTapahtumaRepository.findMasterIdsByHenkilo("1.2.3.4.9");
        assertEquals(0, tapahtumas.size());
    }

    @Test
    public void findByHenkiloInOrganisaatioTest() throws Exception {
        LocalDate voimassaPaattyen = new LocalDate().plusMonths(2);

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA").withKuvaus(text("FI", "Kuvaus"))
                        .withOikeus(oikeus("HENKILOHALLINTA", "CRUD"))
                        .withOikeus(oikeus("KOODISTO", "READ")))
                .voimassaPaattyen(voimassaPaattyen));

        List<MyonnettyKayttoOikeusDto> list = myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByHenkiloInOrganisaatio("1.2.3.4.5", "3.4.5.6.7");
        assertEquals(1, list.size());
        assertEquals("3.4.5.6.7", list.get(0).getOrganisaatioOid());
        assertEquals(KayttoOikeudenTila.MYONNETTY, list.get(0).getTila());
        assertEquals(voimassaPaattyen, list.get(0).getVoimassaPvm());
        assertEquals("1.2.3.4.5", list.get(0).getKasittelijaOid());

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "4.5.6.7.8").tehtavanimike("testaaja"),
                kayttoOikeusRyhma("RYHMA2")
                        .withOikeus(oikeus("KOODISTO", "WRITE")))
                .voimassaPaattyen(new LocalDate().plusMonths(3)));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "4.5.6.7.9").tehtavanimike("testaaja"),
                kayttoOikeusRyhma("RYHMA2")
                        .withOikeus(oikeus("KOODISTO", "WRITE"))
                        .asHidden())
                .voimassaPaattyen(new LocalDate().plusMonths(3)));

        list = myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByHenkiloInOrganisaatio("1.2.3.4.5", null);
        assertEquals(3, list.size());

        list = myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByHenkiloInOrganisaatio("1.2.3.4.5", "3.4.5.6.7");
        assertEquals(1, list.size());
    }
}
