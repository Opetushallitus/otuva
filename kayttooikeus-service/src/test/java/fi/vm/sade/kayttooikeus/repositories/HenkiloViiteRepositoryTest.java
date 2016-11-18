package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.HenkiloViite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class HenkiloViiteRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private HenkiloViiteRepository henkiloViiteRepository;

    @Test
    public void getAllOidsForSamePersonSlave() throws Exception {
        HenkiloViite henkiloViite = new HenkiloViite();
        henkiloViite.setMasterOid("1.2.3.4.5");
        henkiloViite.setSlaveOid("1.2.3.4.1");
        this.em.persist(henkiloViite);

        List<String> allOids = this.henkiloViiteRepository.getAllOidsForSamePerson("1.2.3.4.1")
                .stream().sorted(String::compareToIgnoreCase).collect(Collectors.toList());

        assertThat(allOids.size()).isEqualTo(2);
        assertThat(allOids.get(0)).isEqualTo("1.2.3.4.1");
        assertThat(allOids.get(1)).isEqualTo("1.2.3.4.5");
    }

    @Test
    public void getAllOidsForSamePersonMaster() throws Exception {
        HenkiloViite henkiloViite = new HenkiloViite();
        henkiloViite.setMasterOid("1.2.3.4.5");
        henkiloViite.setSlaveOid("1.2.3.4.1");
        this.em.persist(henkiloViite);

        List<String> allOids = this.henkiloViiteRepository.getAllOidsForSamePerson("1.2.3.4.5")
                .stream().sorted(String::compareToIgnoreCase).collect(Collectors.toList());

        assertThat(allOids.size()).isEqualTo(2);
        assertThat(allOids.get(0)).isEqualTo("1.2.3.4.1");
        assertThat(allOids.get(1)).isEqualTo("1.2.3.4.5");
    }

    @Test
    public void getAllOidsForSamePerson() throws Exception {
        HenkiloViite henkiloViite = new HenkiloViite();
        henkiloViite.setMasterOid("1.2.3.4.5");
        henkiloViite.setSlaveOid("1.2.3.4.4");
        this.em.persist(henkiloViite);

        HenkiloViite henkiloViiteMaster = new HenkiloViite();
        henkiloViiteMaster.setMasterOid("1.2.3.4.5");
        henkiloViiteMaster.setSlaveOid("1.2.3.4.6");
        this.em.persist(henkiloViiteMaster);

        List<String> allOids = this.henkiloViiteRepository.getAllOidsForSamePerson("1.2.3.4.4")
                .stream().sorted(String::compareToIgnoreCase).collect(Collectors.toList());

        assertThat(allOids.size()).isEqualTo(3);
        assertThat(allOids.get(0)).isEqualTo("1.2.3.4.4");
        assertThat(allOids.get(1)).isEqualTo("1.2.3.4.5");
        assertThat(allOids.get(2)).isEqualTo("1.2.3.4.6");
    }
}
