package fi.vm.sade.kayttooikeus.service.external.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.config.properties.UrlConfiguration;
import fi.vm.sade.kayttooikeus.service.external.ExternalServiceException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioHakutulos;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import fi.vm.sade.kayttooikeus.util.FunctionalUtils;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.vm.sade.kayttooikeus.service.external.ExternalServiceException.mapper;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.io;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.retrying;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class OrganisaatioClientImpl implements OrganisaatioClient {
    private final CachingRestClient restClient = new CachingRestClient()
            .setClientSubSystemCode("kayttooikeus.kayttooikeuspalvelu-service");
    private final UrlConfiguration urlConfiguration;
    private final String rootOrganizationOid;
    private final ObjectMapper objectMapper;
    private final OrikaBeanMapper orikaBeanMapper;
    private LocalDateTime cacheUpdatedAt;
    private OrganisaatioCache cache;
    
    
    public OrganisaatioClientImpl(UrlConfiguration urlConfiguration,
                                  ObjectMapper objectMapper,
                                  CommonProperties commonProperties,
                                  OrikaBeanMapper orikaBeanMapper) {
        this.urlConfiguration = urlConfiguration;
        this.objectMapper = objectMapper;
        this.rootOrganizationOid = commonProperties.getRootOrganizationOid();
        this.orikaBeanMapper = orikaBeanMapper;
    }

    @Override
    public synchronized List<OrganisaatioPerustieto> refreshCache() {
        // preventing double queued updates...
        if (this.cacheUpdatedAt == null) {
            String haeHierarchyUrl = this.urlConfiguration.url("organisaatio-service.organisaatio.hae");
            String haeRyhmasUrl = this.urlConfiguration.url("organisaatio-service.organisaatio.ryhmat");
            // Add organisations to cache
            List<OrganisaatioPerustieto> organisaatiosWithoutRootOrg =
                    retrying(io(() -> this.restClient.get(haeHierarchyUrl, OrganisaatioHakutulos.class)), 2)
                            .get().orFail(mapper(haeHierarchyUrl)).getOrganisaatiot();
            // Add ryhmas to cache
            organisaatiosWithoutRootOrg.addAll(Arrays.stream(retrying(io(() ->
                    this.restClient.get(haeRyhmasUrl, OrganisaatioPerustieto[].class)), 2)
                    .get().<ExternalServiceException>orFail(mapper(haeRyhmasUrl)))
                    .map(ryhma -> {
                        // Make ryhma oidpath look same as normal organisations.
                        ryhma.setParentOidPath(ryhma.getParentOidPath()
                                .replaceAll("^\\||\\|$", "")
                                .replace("|", "/") + "/" + ryhma.getOid());
                        return ryhma;
                    }).collect(Collectors.toSet()));
            this.cache = new OrganisaatioCache(this.fetchPerustiedot(this.rootOrganizationOid), organisaatiosWithoutRootOrg);
            this.cacheUpdatedAt = LocalDateTime.now();
            return organisaatiosWithoutRootOrg;
        }
        return Lists.newArrayList();
    }
    
    @Override
    public Optional<OrganisaatioPerustieto> getOrganisaatioPerustiedotCached(String oid) {
        return this.cache.getByOid(oid);
    }

    private OrganisaatioPerustieto fetchPerustiedot(String oid) {
        String url = urlConfiguration.url("organisaatio-service.organisaatio.perustiedot", oid);
        return this.orikaBeanMapper.map(retrying(io(() -> (OrganisaatioRDTO) objectMapper.readerFor(OrganisaatioRDTO.class)
                    .readValue(restClient.getAsString(url))), 2).get().orFail(mapper(url)), OrganisaatioPerustieto.class);
    }

    @Override
    public List<OrganisaatioPerustieto> listActiveOganisaatioPerustiedotRecursiveCached(String organisaatioOid) {
        return this.cache.flatWithParentsAndChildren(organisaatioOid)
                .filter(org -> !rootOrganizationOid.equals(org.getOid())) // the resource never returns the root
                .collect(toList());
    }

    @Override
    public List<OrganisaatioPerustieto> listActiveOrganisaatioPerustiedotByOidRestrictionList(Collection<String> organisaatioOids) {
        if (organisaatioOids.isEmpty()) {
            return new ArrayList<>();
        }
        String url = urlConfiguration.url("organisaatio-service.organisaatio.hae");
        String params = "?oidRestrictionList="+organisaatioOids.stream().collect(joining("&oidRestrictionList=")) +
                ("&aktiiviset=true");
        return retrying(io(() -> restClient.get(url+params,OrganisaatioHakutulos.class)), 2)
                .get().orFail(mapper(url)).getOrganisaatiot();
    }

    @Override
    public List<String> getParentOids(String oid) {
        return this.cache.flatWithParentsByOid(oid).map(OrganisaatioPerustieto::getOid).collect(toList());
    }

    @Override
    public List<String> getChildOids(String oid) {
        return this.cache.flatWithChildrenByOid(oid)
                        .map(OrganisaatioPerustieto::getOid)
                        .collect(toList());
    }

}
