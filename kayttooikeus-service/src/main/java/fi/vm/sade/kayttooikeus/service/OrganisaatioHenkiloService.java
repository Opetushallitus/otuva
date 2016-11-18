package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloListDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloDto;
import fi.vm.sade.kayttooikeus.model.HenkiloTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;

import java.util.List;

public interface OrganisaatioHenkiloService {
    
    List<OrganisaatioHenkiloListDto> listOrganisaatioHenkilos(String henkiloOid, String compareByLang);
    
    List<OrganisaatioPerustieto> listOrganisaatioPerustiedotForCurrentUser();

    List<HenkiloTyyppi> listPossibleHenkiloTypesAccessibleForCurrentUser();

    OrganisaatioHenkiloDto findOrganisaatioHenkiloByHenkiloAndOrganisaatio(String henkiloOid, String organisaatioOid);

    List<OrganisaatioHenkiloDto> findOrganisaatioByHenkilo(String henkiloOid);
}
