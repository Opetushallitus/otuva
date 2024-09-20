package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface KayttooikeusryhmaDataRepository extends CrudRepository<KayttoOikeusRyhma, Long> {
    @NonNull Optional<KayttoOikeusRyhma> findById(@NonNull Long id);
}
