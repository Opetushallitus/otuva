package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.model.HenkiloTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;

import java.util.List;

/**
 * User: tommiratamaa
 * Date: 12/10/2016
 * Time: 14.37
 */
public interface OrganisaatioHenkiloService {
    
    List<OrganisaatioPerustieto> listOrganisaatioPerustiedotForCurrentUser();

    List<HenkiloTyyppi> listPossibleHenkiloTypesAccessibleForCurrentUser();
}
