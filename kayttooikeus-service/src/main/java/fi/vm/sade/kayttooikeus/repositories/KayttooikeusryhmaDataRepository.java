package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface KayttooikeusryhmaDataRepository extends org.springframework.data.repository.Repository<KayttoOikeusRyhma, Long> {
    Optional<KayttoOikeusRyhma> findOne(Long id);
}
