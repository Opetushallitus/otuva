package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.model.HenkiloTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;

import java.util.List;

public interface OrganisaatioHenkiloService {
    
    List<OrganisaatioPerustieto> listOrganisaatioPerustiedotForCurrentUser();

    List<HenkiloTyyppi> listPossibleHenkiloTypesAccessibleForCurrentUser();
}
