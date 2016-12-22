package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.dto.KayttajatiedotReadDto;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class KayttajatiedotRepositoryImplTest {

    @Autowired
    private KayttajatiedotRepositoryImpl repository;

    @Test
    public void testQueryExecutes() {
        String oid = "oid1";

        Optional<KayttajatiedotReadDto> kayttajatiedot = repository.findByHenkiloOid(oid);

        assertThat(kayttajatiedot).isEmpty();
    }

}