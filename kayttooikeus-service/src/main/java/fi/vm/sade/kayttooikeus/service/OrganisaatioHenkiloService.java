package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface OrganisaatioHenkiloService {

    List<OrganisaatioHenkiloWithOrganisaatioDto> listOrganisaatioHenkilos(String henkiloOid, String compareByLang, PalveluRooliGroup requiredRoles);

    Collection<String> listOrganisaatioOidBy(OrganisaatioHenkiloCriteria criteria);

    OrganisaatioHenkiloDto findOrganisaatioHenkiloByHenkiloAndOrganisaatio(String henkiloOid, String organisaatioOid);

    List<OrganisaatioHenkiloDto> findOrganisaatioByHenkilo(String henkiloOid);

    // Change organisaatiohenkilo passive and close all related myonnettykayttooikeusryhmatapahtumas
    void passivoiHenkiloOrganisation(String oidHenkilo, String henkiloOrganisationOid);


    // Passivoi organisaatiohenkilot joiden organisaatio on passivoitu ja poistaa näiltä organisaatiohenkilöiltä kaikki käyttöoikeudet
    void kasitteleOrganisaatioidenLakkautus(String kasittelijaOid);

    Set<HenkilohakuResultDto> addOrganisaatioInformation(Set<HenkilohakuResultDto> set);
}
