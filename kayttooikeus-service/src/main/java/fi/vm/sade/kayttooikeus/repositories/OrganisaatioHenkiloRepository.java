package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloListDto;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloDto;

import java.util.List;
import java.util.Optional;

public interface OrganisaatioHenkiloRepository {
    List<String> findDistinctOrganisaatiosForHenkiloOid(String henkiloOid);
    
    List<OrganisaatioHenkiloListDto> findOrganisaatioHenkiloListDtos(String henkiloOoid);

    Optional<OrganisaatioHenkiloDto> findByHenkiloOidAndOrganisaatioOid(String henkiloOid, String organisaatioOid);

    List<OrganisaatioHenkiloDto> findOrganisaatioHenkilosForHenkilo(String henkiloOid);
}
