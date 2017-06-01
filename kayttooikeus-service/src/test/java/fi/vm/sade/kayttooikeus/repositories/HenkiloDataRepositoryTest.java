package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@Transactional(readOnly = true)
public class HenkiloDataRepositoryTest {
    @Autowired
    HenkiloDataRepository henkiloDataRepository;

    @Autowired
    TestEntityManager testEntityManager;

    @Test
    public void findByOidHenkilo() {
        Henkilo henkilo = new Henkilo();
        henkilo.setHenkiloTyyppi(HenkiloTyyppi.OPPIJA);
        henkilo.setOidHenkilo("1.2.3.4.5");
        this.testEntityManager.persist(henkilo);

        Optional<Henkilo> returnHenkilo = this.henkiloDataRepository.findByOidHenkilo("1.2.3.4.5");
        assertThat(returnHenkilo).hasValueSatisfying(h -> assertThat(h.getOidHenkilo()).isEqualTo("1.2.3.4.5"));
    }

    @Test
    public void findByOidHenkiloNotFound() {
        Optional<Henkilo> returnHenkilo = this.henkiloDataRepository.findByOidHenkilo("1.2.3.4.5");
        assertThat(returnHenkilo).isEmpty();
    }
}
