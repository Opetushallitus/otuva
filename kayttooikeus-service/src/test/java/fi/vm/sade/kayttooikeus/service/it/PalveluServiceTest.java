package fi.vm.sade.kayttooikeus.service.it;


import fi.vm.sade.kayttooikeus.dto.PalveluDto;
import fi.vm.sade.kayttooikeus.model.Palvelu;
import fi.vm.sade.kayttooikeus.service.PalveluService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static fi.vm.sade.kayttooikeus.repositories.populate.PalveluPopulator.palvelu;
import static fi.vm.sade.kayttooikeus.repositories.populate.TextGroupPopulator.text;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@Sql("/truncate_tables.sql")
public class PalveluServiceTest extends AbstractServiceIntegrationTest {
    @Autowired
    private PalveluService palveluService;

    @Test
    public void listPalvelusTest() {
        Palvelu palvelu1 = populate(palvelu("HENKILOPALVELU").kuvaus(text("FI", "Henkilöpalvelu")
                                .put("EN", "Person service")));
        populate(palvelu("KOODISTO"));
        List<PalveluDto> palvelus = palveluService.listPalvelus();
        assertEquals(2, palvelus.size());
        assertEquals(palvelu1.getId(), palvelus.get(0).getId());
        assertEquals("Henkilöpalvelu", palvelus.get(0).getDescription().get("fi"));
        assertEquals("KOODISTO", palvelus.get(1).getName());
    }
}
