package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.OrganisaatioCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisaatioCacheRepository extends JpaRepository<OrganisaatioCache, Long>, OrganisaatioCacheRepositoryCustom {
}
