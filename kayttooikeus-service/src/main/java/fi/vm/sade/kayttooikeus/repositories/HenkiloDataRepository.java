package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(propagation = Propagation.MANDATORY)
@Repository
public interface HenkiloDataRepository extends CrudRepository<Henkilo, Long> {
    Optional<Henkilo> findByOidHenkilo(String oidHenkilo);

    @Transactional(propagation = Propagation.SUPPORTS)
    Long countByEtunimetCachedNotNull();

    List<Henkilo> findByOidHenkiloIn(List<String> oidHenkilo);

    @EntityGraph("henkilohaku")
    List<Henkilo> readByOidHenkiloIn(List<String> oidHenkilo);

}
