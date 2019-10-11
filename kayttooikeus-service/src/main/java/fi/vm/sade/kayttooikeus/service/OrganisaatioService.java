package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioCriteriaDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloWithOrganisaatioDto.OrganisaatioDto;

import java.util.Collection;

public interface OrganisaatioService {

    void updateOrganisaatioCache();

    Long getClientCacheState();

    Collection<OrganisaatioDto> listBy(OrganisaatioCriteriaDto criteria);

    OrganisaatioDto getRootWithChildrenBy(OrganisaatioCriteriaDto criteria);

    OrganisaatioDto getByOid(String oid);
}
