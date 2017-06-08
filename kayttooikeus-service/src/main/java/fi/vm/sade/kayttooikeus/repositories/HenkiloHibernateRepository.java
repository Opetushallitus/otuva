package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.repositories.criteria.HenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
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

    List<HenkilohakuResultDto> findByCriteria(HenkiloCriteria criteria);

    List<String> findHenkiloOids(HenkiloTyyppi henkiloTyyppi, List<String> ooids, String groupName);

    /**
     * Palauttaa henkilöt jotka kuuluvat johonkin annettuun käyttöoikeusryhmään
     * ja organisaatioon.
     *
     * @param kayttoOikeusRyhmaIds käyttöoikeusryhmät
     * @param organisaatioOids organisaatiot
     * @return henkilöt
     */
    List<Henkilo> findByKayttoOikeusRyhmatAndOrganisaatiot(Set<Long> kayttoOikeusRyhmaIds, Set<String> organisaatioOids);

    /**
     * Palauttaa henkilöt joilla on myönnettynä annettu käyttöoikeusryhmä.
     *
     * @param kayttoOikeusRyhmaId käyttöoikeusryhmän id
     * @return henkilöt
     */
    Set<String> findOidsByKayttoOikeusRyhmaId(Long kayttoOikeusRyhmaId);

    /**
     * Palauttaa henkilöiden oid:t joilta löytyy käyttäjätunnus.
     *
     * @return henkilö oid:t
     */
    Set<String> findOidsByHavingUsername();
}
