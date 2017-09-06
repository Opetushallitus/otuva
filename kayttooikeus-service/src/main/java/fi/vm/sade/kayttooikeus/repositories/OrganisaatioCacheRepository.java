package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.OrganisaatioCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganisaatioCacheRepository extends JpaRepository<OrganisaatioCache, Long>, OrganisaatioCacheRepositoryCustom {
    Optional<OrganisaatioCache> findByOrganisaatioOid(String organisaatioOid);
}
