package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface OrganisaatioHenkiloRepository extends CrudRepository<OrganisaatioHenkilo, Long>, OrganisaatioHenkiloCustomRepository {

    List<OrganisaatioHenkilo> findByHenkilo(Henkilo henkilo);

    List<OrganisaatioHenkilo> findByHenkiloOidHenkilo(String oidHenkilo);

    Optional<OrganisaatioHenkilo> findByHenkiloOidHenkiloAndOrganisaatioOid(String oidHenkilo, String organisaatioOid);

    List<OrganisaatioHenkilo> findByOrganisaatioOidIn(Set<String> organisaatioOids);

    @Query("""
        select h.oidHenkilo, o.organisaatioOid
        from OrganisaatioHenkilo o
        inner join Henkilo h on h.id = o.henkilo.id and h.oidHenkilo in :henkiloOids
        where o.passivoitu = false
    """)
    List<String[]> findActiveByHenkiloOids0(@Param("henkiloOids") List<String> henkiloOids);

    default Map<String, List<String>> findActiveByHenkiloOids(List<String> henkiloOids) {
        return findActiveByHenkiloOids0(henkiloOids).stream()
            .collect(
                Collectors.groupingBy(o -> o[0], Collectors.mapping(o -> o[1], Collectors.toList()))
            );
    }
}
