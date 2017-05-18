package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.AnomuksenTila;
import fi.vm.sade.kayttooikeus.model.HaettuKayttoOikeusRyhma;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface HaettuKayttooikeusRyhmaDataRepository extends CrudRepository<HaettuKayttoOikeusRyhma, Long> {

    List<HaettuKayttoOikeusRyhma> findByAnomusHenkiloOidHenkiloAndAnomusAnomuksenTila(String oidHenkilo, AnomuksenTila anomuksenTila);

    List<HaettuKayttoOikeusRyhma> findByAnomusHenkiloOidHenkilo(String oidHenkilo);

    Optional<HaettuKayttoOikeusRyhma> findById(Long id);

}
