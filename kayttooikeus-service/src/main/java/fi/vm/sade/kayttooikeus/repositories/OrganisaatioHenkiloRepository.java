package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloDto;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;

import java.util.List;
import java.util.Optional;

public interface OrganisaatioHenkiloRepository extends BaseRepository<OrganisaatioHenkilo> {
    List<String> findDistinctOrganisaatiosForHenkiloOid(String henkiloOid);

    Optional<OrganisaatioHenkiloDto> findByHenkiloOidAndOrganisaatioOid(String henkiloOid, String organisaatioOid);

    List<OrganisaatioHenkiloDto> findOrganisaatioHenkilosForHenkilo(String henkiloOid);
}
