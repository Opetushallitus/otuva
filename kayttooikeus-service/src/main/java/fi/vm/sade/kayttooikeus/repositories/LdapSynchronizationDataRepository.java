package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.LdapSynchronizationData;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LdapSynchronizationDataRepository extends JpaRepository<LdapSynchronizationData, Long> {

    /**
     * Palauttaa uusimman synkronoinnin tiedot.
     *
     * @return synkronointitieto
     */
    Optional<LdapSynchronizationData> findFirstByOrderByIdDesc();

    /**
     * Palauttaa synkronointitiedot vanhimmasta uusimpaan.
     *
     * @return synkronointiedot
     */
    List<LdapSynchronizationData> findByOrderByIdAsc();

}
