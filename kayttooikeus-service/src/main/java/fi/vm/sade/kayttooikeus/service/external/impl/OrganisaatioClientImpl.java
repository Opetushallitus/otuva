package fi.vm.sade.kayttooikeus.service.external.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.config.properties.UrlConfiguration;
import fi.vm.sade.kayttooikeus.service.external.ExternalServiceException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioHakutulos;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioStatus;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;

import java.util.*;
import java.util.stream.Collectors;

import static fi.vm.sade.kayttooikeus.service.external.ExternalServiceException.mapper;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.io;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.retrying;
import static java.util.stream.Collectors.toList;

public class OrganisaatioClientImpl implements OrganisaatioClient {
    private final CachingRestClient restClient = new CachingRestClient()
            .setClientSubSystemCode("kayttooikeus.kayttooikeuspalvelu-service");
    private final UrlConfiguration urlConfiguration;
    private final String rootOrganizationOid;
    private final ObjectMapper objectMapper;
    private final OrikaBeanMapper orikaBeanMapper;

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
        Map<String, String> queryParamsAktiivisetSuunnitellut = new HashMap<String, String>() {{
            put("aktiiviset", "true");
            put("suunnitellut", "true");
            put("lakkautetut", "true");
        }};
        String haeHierarchyUrl = this.urlConfiguration.url("organisaatio-service.organisaatio.v2.hae", queryParamsAktiivisetSuunnitellut);
        // Add organisations to cache (active, incoming and passive)
        List<OrganisaatioPerustieto> organisaatiosWithoutRootOrg =
                retrying(io(() -> this.restClient.get(haeHierarchyUrl, OrganisaatioHakutulos.class)), 2)
                        .get().orFail(mapper(haeHierarchyUrl)).getOrganisaatiot();
        // Add ryhmas to cache
        String haeRyhmasUrl = this.urlConfiguration.url("organisaatio-service.organisaatio.ryhmat");
        organisaatiosWithoutRootOrg.addAll(Arrays.stream(retrying(io(() ->
                this.restClient.get(haeRyhmasUrl, OrganisaatioPerustieto[].class)), 2)
                .get().<ExternalServiceException>orFail(mapper(haeRyhmasUrl)))
                // Make ryhma parentoidpath format same as on normal organisations.
                .map(ryhma -> {
                    ryhma.setParentOidPath(ryhma.getOid() + "/"
                            + ryhma.getParentOidPath().replaceAll("^\\||\\|$", "").replace("|", "/"));
                    return ryhma;
                }).collect(Collectors.toSet()));
        this.cache = new OrganisaatioCache(this.fetchPerustiedot(this.rootOrganizationOid), organisaatiosWithoutRootOrg);
        return organisaatiosWithoutRootOrg;
    }

    private OrganisaatioPerustieto fetchPerustiedot(String oid) {
        String url = urlConfiguration.url("organisaatio-service.organisaatio.perustiedot", oid);
        return this.orikaBeanMapper.map(retrying(io(() -> (OrganisaatioRDTO) objectMapper.readerFor(OrganisaatioRDTO.class)
                .readValue(restClient.getAsString(url))), 2).get().orFail(mapper(url)), OrganisaatioPerustieto.class);
    }

    @Override
    public Optional<OrganisaatioPerustieto> getOrganisaatioPerustiedotCached(String oid) {
        return this.cache.getByOid(oid);
    }

    @Override
    public List<OrganisaatioPerustieto> listActiveOganisaatioPerustiedotRecursiveCached(String organisaatioOid) {
        return this.cache.flatWithParentsAndChildren(organisaatioOid)
                .filter(org -> !rootOrganizationOid.equals(org.getOid())) // the resource never returns the root
                .collect(toList());
    }

    @Override
    public List<OrganisaatioPerustieto> listActiveOrganisaatioPerustiedotByOidRestrictionList(Collection<String> organisaatioOids) {
        return organisaatioOids.stream()
                .map(this.cache::getByOid)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(organisaatioPerustieto -> OrganisaatioStatus.AKTIIVINEN.name().equals(organisaatioPerustieto.getStatus()))
                .filter(organisaatioPerustieto -> organisaatioPerustieto.getAlkuPvm() == null || organisaatioPerustieto.getAlkuPvm().before(new Date()))
                .filter(organisaatioPerustieto -> organisaatioPerustieto.getLakkautusPvm() == null || organisaatioPerustieto.getLakkautusPvm().after(new Date()))
                .collect(Collectors.toList());
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
