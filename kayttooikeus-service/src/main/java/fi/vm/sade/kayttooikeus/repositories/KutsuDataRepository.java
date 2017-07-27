package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.KutsunTila;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface KutsuDataRepository extends JpaRepository<Kutsu, Long> {
    List<Kutsu> findByTilaAndKutsuja(Sort sort, KutsunTila tila, String kutsujaOid);

    List<Kutsu> findByTila(Sort sort, KutsunTila tila);

    Optional<Kutsu> findByTemporaryTokenAndTila(String temporaryToken, KutsunTila kutsunTila);

}
