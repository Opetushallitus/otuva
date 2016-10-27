package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.KayttoOikeus;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.Text;
import fi.vm.sade.kayttooikeus.model.TextGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.constraints.AssertFalse;
import java.util.Arrays;
import java.util.List;

import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusPopulator.oikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.viite;
import static fi.vm.sade.kayttooikeus.repositories.populate.TextGroupPopulator.text;
import static org.junit.Assert.*;

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

    @Test
    public void findByIdListTest(){
        Long id1 =populate(kayttoOikeusRyhma("RYHMÄ")
                .withOikeus(oikeus("APP1", "READ"))
                .withOikeus(oikeus("APP2", "WRITE"))).getId();

        Long id2 = populate(kayttoOikeusRyhma("RYHMÄ2")
                .withOikeus(oikeus("APP1", "READ"))
                .withOikeus(oikeus("APP2", "WRITE"))).getId();

        Long id3 = populate(kayttoOikeusRyhma("RYHMÄ2")
                .asHidden()
                .withOikeus(oikeus("APP1", "READ"))
                .withOikeus(oikeus("APP2", "WRITE"))).getId();

        List<KayttoOikeusRyhma> ryhmas = kayttoOikeusRyhmaRepository.findByIdList(Arrays.asList(id2));
        assertEquals(1, ryhmas.size());
        assertEquals("RYHMÄ2", ryhmas.get(0).getName());

        ryhmas = kayttoOikeusRyhmaRepository.findByIdList(Arrays.asList(id1, id2));
        assertEquals(2, ryhmas.size());

        ryhmas = kayttoOikeusRyhmaRepository.findByIdList(Arrays.asList(id1, id2, id3));
        assertEquals(2, ryhmas.size());

        ryhmas = kayttoOikeusRyhmaRepository.findByIdList(Arrays.asList(id1, id2, id2));
        assertEquals(2, ryhmas.size());

        ryhmas = kayttoOikeusRyhmaRepository.findByIdList(Arrays.asList(id1, id2, 675467546L));
        assertEquals(2, ryhmas.size());
    }

    @Test
    public void findByIdTest(){
        Long id1 =populate(kayttoOikeusRyhma("RYHMÄ")
                .withKuvaus(text("FI", "Kuvaus"))
                .withViite(viite(kayttoOikeusRyhma("RYHMA1"), "TYYPPI"))
                .withOikeus(oikeus("APP1", "READ"))
                .withOikeus(oikeus("APP2", "WRITE"))).getId();

        Long id3 = populate(kayttoOikeusRyhma("RYHMÄ2")
                .asHidden()
                .withOikeus(oikeus("APP1", "READ"))
                .withOikeus(oikeus("APP2", "WRITE"))).getId();

        KayttoOikeusRyhma ryhma = kayttoOikeusRyhmaRepository.findById(id1);
        assertEquals("RYHMÄ", ryhma.getName());
        assertEquals(1, ryhma.getOrganisaatioViite().size());
        assertEquals("RYHMA1", ryhma.getOrganisaatioViite().iterator().next().getKayttoOikeusRyhma().getName());
        assertEquals("Kuvaus", ryhma.getDescription().getTexts().stream().filter(text -> text.getLang().equals("FI")).findFirst().get().getText());
        assertEquals(2, ryhma.getKayttoOikeus().size());

        ryhma = kayttoOikeusRyhmaRepository.findById(id3);
        assertNull(ryhma);

        ryhma = kayttoOikeusRyhmaRepository.findById(434323423L);
        assertNull(ryhma);
    }


    @Test
    public void ryhmaNameExistTest(){
        populate(kayttoOikeusRyhma("RYHMÄ")
                .withKuvaus(text("FI", "Kuvaus")));

        populate(kayttoOikeusRyhma("RYHMÄ")
                .withKuvaus(text("EN", "Kuvaus en")));

        Boolean found = kayttoOikeusRyhmaRepository.ryhmaNameFiExists("Kuvaus");
        assertTrue(found);

        found = kayttoOikeusRyhmaRepository.ryhmaNameFiExists("Kuvaus en");
        assertFalse(found);

        found = kayttoOikeusRyhmaRepository.ryhmaNameFiExists("madeup");
        assertFalse(found);
    }

    @Test
    public void insertTest(){

        KayttoOikeus oikeus = populate(oikeus("APP1", "READ"));
        List<KayttoOikeusRyhma> ryhmas = kayttoOikeusRyhmaRepository.listAll();
        assertEquals(0, ryhmas.size());

        KayttoOikeusRyhma kor = new KayttoOikeusRyhma();
        kor.setName("TEST");
        kor.setHidden(false);
        TextGroup textGroup = new TextGroup();
        textGroup.addText(new Text(textGroup, "FI", "kuvaus"));
        kor.setDescription(textGroup);
        kor.getKayttoOikeus().add(oikeus);
        kor.setRooliRajoite("roolirajoite");
        kor = kayttoOikeusRyhmaRepository.insert(kor);

        ryhmas = kayttoOikeusRyhmaRepository.listAll();
        assertEquals(1, ryhmas.size());

        KayttoOikeusRyhma ryhma = kayttoOikeusRyhmaRepository.findById(kor.getId());
        assertEquals("TEST", ryhma.getName());
        assertEquals("roolirajoite", ryhma.getRooliRajoite());
        assertEquals("kuvaus", ryhma.getDescription().getTexts().stream().findFirst().get().getText());
        assertEquals("APP1", ryhma.getKayttoOikeus().iterator().next().getPalvelu().getName());
        assertEquals("READ", ryhma.getKayttoOikeus().iterator().next().getRooli());
    }


}
