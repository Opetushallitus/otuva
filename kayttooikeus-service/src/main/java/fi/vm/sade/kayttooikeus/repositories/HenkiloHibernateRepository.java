package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.HenkiloTyyppi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface HenkiloHibernateRepository extends BaseRepository<Henkilo> {
    List<String> findHenkiloOids(HenkiloTyyppi henkiloTyyppi, List<String> ooids, String groupName);
}
