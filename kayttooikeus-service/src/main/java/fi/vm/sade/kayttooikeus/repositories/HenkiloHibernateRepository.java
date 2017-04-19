package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface HenkiloHibernateRepository extends BaseRepository<Henkilo> {

    /**
     * Palauttaa hakukriteerien mukaiset henkilöiden oid:t.
     *
     * @param criteria hakukriteerit
     * @return henkilö oid:t
     */
    Set<String> findOidsBy(OrganisaatioHenkiloCriteria criteria);

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
