package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloListDto;

import java.util.List;

public interface OrganisaatioHenkiloRepository {
    List<String> findDistinctOrganisaatiosForHenkiloOid(String henkiloOid);
    
    List<OrganisaatioHenkiloListDto> findOrganisaatioHenkiloListDtos(String henkiloOoid);
}
