package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.AnomuksenTila;
import fi.vm.sade.kayttooikeus.repositories.AbstractRepositoryTest;
import fi.vm.sade.kayttooikeus.repositories.AnomusRepositoryCustom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.Period;

import static fi.vm.sade.kayttooikeus.repositories.populate.AnomusPopulator.anomus;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class AnomusRepositoryImplTest extends AbstractRepositoryTest {

    @Autowired
    private AnomusRepositoryCustom anomusRepository;

    @Test
    public void findExpiredApplications() {
        populate(anomus("")
                .tila(AnomuksenTila.ANOTTU)
                .anottuPvm(LocalDateTime.now().minus(Period.ofMonths(1)))
        );
        assertThat(anomusRepository.findExpired(Period.ZERO)).hasSize(1);
        assertThat(anomusRepository.findExpired(Period.ofMonths(2))).isEmpty();
    };

    @Test
    public void findExpiredApplicationsIgnoreWrongStatus() {
        populate(anomus("")
                .tila(AnomuksenTila.HYLATTY)
                .anottuPvm(LocalDateTime.now().minus(Period.ofMonths(1)))
        );
        assertThat(anomusRepository.findExpired(Period.ZERO)).isEmpty();
    }
}
