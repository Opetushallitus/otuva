package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioCriteriaDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioWithChildrenDto;

import java.util.Collection;

public interface OrganisaatioService {

    void updateOrganisaatioCache();

    Long getClientCacheState();

    Collection<OrganisaatioWithChildrenDto> listBy(OrganisaatioCriteriaDto criteria);

    OrganisaatioWithChildrenDto getRootWithChildrenBy(OrganisaatioCriteriaDto criteria);

    OrganisaatioWithChildrenDto getByOid(String oid);
}
