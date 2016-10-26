package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.PalveluDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.List;

import static fi.vm.sade.kayttooikeus.repositories.populate.PalveluPopulator.palvelu;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class PalveluRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private PalveluRepository palveluRepository;

    @Test
    public void findAllTest() {
        populate(palvelu("PALVELU1"));
        populate(palvelu("PALVELU2"));
        
        List<PalveluDto> palvelus = palveluRepository.findAll();
        assertEquals(2, palvelus.size());
        assertEquals(new HashSet<>(asList("PALVELU1", "PALVELU2")),
                palvelus.stream().map(PalveluDto::getName).collect(toSet()));
    }
}
