package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.TextGroup;
import fi.vm.sade.kayttooikeus.repositories.dto.TextGroupTextDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static fi.vm.sade.kayttooikeus.repositories.populate.TextGroupPopulator.text;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class TextGroupRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TextGroupRepository textGroupRepository;

    @Test
    public void findTextsTest() {
        populate(text("FI", "Testi2")
                    .put("EN", "Test2"));
        TextGroup g1 = populate(text("FI", "Testi1")
                    .put("EN", "Test1")
                    .put("SV", "Test1")),
            g3 = populate(text("FI", "Testi3")
                .put("EN", "Test3"));

        List<TextGroupTextDto> results = textGroupRepository.findTexts(asList(g1.getId(), g3.getId()));
        assertEquals(5, results.size());
        assertEquals("EN", results.get(0).getLang());
        assertEquals("Test1", results.get(0).getText());
        assertEquals("SV", results.get(2).getLang());
        assertEquals("Test1", results.get(2).getText());
        assertEquals("FI", results.get(4).getLang());
        assertEquals("Testi3", results.get(4).getText());
    }

}
