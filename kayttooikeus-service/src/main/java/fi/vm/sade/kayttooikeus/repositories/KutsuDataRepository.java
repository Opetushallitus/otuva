package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.KutsunTila;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface KutsuDataRepository extends JpaRepository<Kutsu, Long> {
    @Query("select k from Kutsu k where k.tila = ?1 AND k.kutsuja = ?2 " +
            "AND (?3 = '' OR LOWER(CONCAT(k.etunimi, ' ', k.sukunimi, ' ', k.etunimi)) like CONCAT('%', LOWER(?3), '%')) " +
            "AND (?4 IS NULL OR ?4 in k.organisaatiot)")
    List<Kutsu> findByTilaAndKutsujaAndNimetContainingAAndOrganisaatiotContains(Sort sort, KutsunTila tila, String kutsujaOid, String queryTerm, String organisaatioOid);

    @Query("select k from Kutsu k where k.tila = ?1 " +
            "AND (?2 = '' OR LOWER(CONCAT(k.etunimi, ' ', k.sukunimi, ' ', k.etunimi)) like CONCAT('%', LOWER(?2), '%')) ")
    List<Kutsu> findByTilaAndNimetContaining(Sort sort, KutsunTila tila, String queryTerm, String organisaatioOid);

}
