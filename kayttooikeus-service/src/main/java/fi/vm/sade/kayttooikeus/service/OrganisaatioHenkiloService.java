package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloCreateDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloDto;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;

import java.util.List;

public interface OrganisaatioHenkiloService {
    
    List<OrganisaatioPerustieto> listOrganisaatioPerustiedotForCurrentUser();

    List<HenkiloTyyppi> listPossibleHenkiloTypesAccessibleForCurrentUser();

    OrganisaatioHenkiloDto findOrganisaatioHenkiloByHenkiloAndOrganisaatio(String henkiloOid, String organisaatioOid);

    List<OrganisaatioHenkiloDto> findOrganisaatioByHenkilo(String henkiloOid);

    /**
     * Lisää uudet organisaatiot henkilölle. Ei päivitä tai poista vanhoja
     * organisaatiotietoja.
     *
     * @param henkiloOid henkilö oid
     * @param organisaatioHenkilot henkilön organisaatiotiedot
     * @return kaikki henkilön organisaatiotiedot
     */
    List<OrganisaatioHenkiloDto> addOrganisaatioHenkilot(String henkiloOid, List<OrganisaatioHenkiloCreateDto> organisaatioHenkilot);
}
