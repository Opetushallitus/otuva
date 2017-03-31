package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface HenkiloHibernateRepository extends BaseRepository<Henkilo> {

    /**
     * Palauttaa organisaatioon kuuluvien henkilöiden oid:t.
     *
     * @param organisaatioOid organisaatio oid
     * @param passivoitu onko henkilö-organisaatio -liitos voimassa
     * @return henkilö oid:t
     */
    Set<String> findOidsByOrganisaatio(String organisaatioOid, Optional<Boolean> passivoitu);

    /**
     * Palauttaa henkilöiden oid:t joilla on sama organisaatio kuin annetulla
     * henkilöllä.
     *
     * @param henkiloOid henkilö oid
     * @param criteria haun lisäehdot
     * @return henkilö oid:t
     */
    Set<String> findOidsBySamaOrganisaatio(String henkiloOid, OrganisaatioHenkiloCriteria criteria);

    List<String> findHenkiloOids(HenkiloTyyppi henkiloTyyppi, List<String> ooids, String groupName);
}
