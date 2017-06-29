package fi.vm.sade.kayttooikeus.service.external.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.config.properties.UrlConfiguration;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.ExternalServiceException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioHakutulos;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import fi.vm.sade.kayttooikeus.util.FunctionalUtils;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static fi.vm.sade.kayttooikeus.service.external.ExternalServiceException.mapper;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.io;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.retrying;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

public class OrganisaatioClientImpl implements OrganisaatioClient {
    private final CachingRestClient restClient = new CachingRestClient()
            .setClientSubSystemCode("kayttooikeus.kayttooikeuspalvelu-service");
    private final UrlConfiguration urlConfiguration;
    private final String rootOrganizationOid;
    private final ObjectMapper objectMapper;
    private final OrikaBeanMapper orikaBeanMapper;
    private LocalDateTime cacheUpdatedAt;
    private LocalDate latestChanges;
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

    protected<T> T cached(Function<OrganisaatioCache, T> fromCache, Supplier<T> direct, Mode mode) {
        if (!mode.isExpectMultiple()) {
            return direct.get();
        }
        if (mode.isChangeChecked()) {
            if (latestChanges != null && latestChanges.equals(LocalDate.now())) {
                return direct.get();
            }
            return fromCache.apply(cache);
        }
        if (cacheUpdatedAt == null || (LocalDate.now().isAfter(cacheUpdatedAt.toLocalDate())
                && changesSince(LocalDate.now().minusDays(2)))) {
            refreshCache(cacheUpdatedAt);
            mode.checked();
            return fromCache.apply(cache);
        } else if (cacheUpdatedAt.toLocalDate().equals(LocalDate.now())
                && changesSince(LocalDate.now().minusDays(1))) {
            // changes today (since the modification date is known only in date precision, 
            // we can't be sure if the modifications today have happened before/after last update) => no cache
            mode.checked();
            return direct.get();
        }
        mode.checked();
        return fromCache.apply(cache);
    }

    private synchronized void refreshCache(LocalDateTime updateMoment) {
        // preventing double queued updates...
        if (cacheUpdatedAt == null || (updateMoment != null && updateMoment.isBefore(cacheUpdatedAt))) {
            String haeHierarchyUrl = urlConfiguration.url("organisaatio-service.organisaatio.hae");
            String haeRyhmasUrl = urlConfiguration.url("organisaatio-service.organisaatio.ryhmat");
            // Add organisations to cache
            List<OrganisaatioPerustieto> organisaatios =
                    retrying(io(() -> restClient.get(haeHierarchyUrl, OrganisaatioHakutulos.class)), 2)
                            .get().orFail(mapper(haeHierarchyUrl)).getOrganisaatiot();
            // Add ryhmas to cache
            organisaatios.addAll(Arrays.asList(retrying(io(() ->
                    restClient.get(haeRyhmasUrl, OrganisaatioPerustieto[].class)), 2)
                    .get().<ExternalServiceException>orFail(mapper(haeRyhmasUrl))));
            cache = new OrganisaatioCache(fetchPerustiedot(rootOrganizationOid), organisaatios);
            cacheUpdatedAt = LocalDateTime.now();
        }
    }
    
    @Getter @Setter
    public static class MuutetutOidListContainer {
        private List<String> oids;
    }

    private boolean changesSince(LocalDate date) {
        String url = urlConfiguration.url("organisaatio-service.organisaatio.muutetut.oid", date.toString());
        List<String> changedOrganisations = retrying(io(() -> (MuutetutOidListContainer) objectMapper.readerFor(MuutetutOidListContainer.class)
                .readValue(restClient.getAsString(url))), 2).get().orFail(mapper(url)).getOids();
        boolean result = !isReallyEmpty(changedOrganisations);
        if (result && (latestChanges == null || latestChanges.isBefore(date.plusDays(1)))) {
            latestChanges = date.plusDays(1);
        }
        return result;
    }
    
    private boolean isReallyEmpty(List<String> oids) {
        // "conveniently" muutetut/oid returns {"oids":[""]} for empty result
        return oids.isEmpty() || oids.get(0).isEmpty();
    }

    @Override
    public Optional<OrganisaatioPerustieto> getOrganisaatioPerustiedotCached(String oid, Mode mode) {
        return cached(c -> c.getByOid(oid),
                () -> Optional.ofNullable(fetchPerustiedotWithChildren(oid)), mode);
    }

    public OrganisaatioPerustieto fetchPerustiedot(String oid) {
        String url = urlConfiguration.url("organisaatio-service.organisaatio.perustiedot", oid);
        return this.orikaBeanMapper.map(retrying(io(() -> (OrganisaatioRDTO) objectMapper.readerFor(OrganisaatioRDTO.class)
                    .readValue(restClient.getAsString(url))), 2).get().orFail(mapper(url)), OrganisaatioPerustieto.class);
    }

    // Works also with root organisation
    public OrganisaatioPerustieto fetchPerustiedotWithChildren(String oid) {
        String perustietoUrl = urlConfiguration.url("organisaatio-service.organisaatio.perustiedot", oid);
        String childrenUrl = urlConfiguration.url("organisaatio-service.organisaatio.children", oid);

        OrganisaatioPerustieto organisaatioPerustieto = this.orikaBeanMapper.map(retrying(io(() -> (OrganisaatioRDTO) objectMapper.readerFor(OrganisaatioRDTO.class)
                        .readValue(restClient.getAsString(perustietoUrl))), 2).get().orFail(mapper(perustietoUrl)),
                OrganisaatioPerustieto.class);
                List<OrganisaatioRDTO> children = retrying(FunctionalUtils.<List<OrganisaatioRDTO>>io(
                        () -> this.objectMapper.readerFor(new TypeReference<List<OrganisaatioRDTO>>() {})
                                .readValue(this.restClient.get(childrenUrl))), 2).get()
                        .orFail((RuntimeException e) -> new ExternalServiceException(childrenUrl, e.getMessage(), e));
        organisaatioPerustieto.setChildren(this.orikaBeanMapper.mapAsList(children, OrganisaatioPerustieto.class));
        return organisaatioPerustieto;
    }

    @Override
    public List<OrganisaatioPerustieto> listWithoutRoot() {
        String url = urlConfiguration.url("organisaatio-service.organisaatio.hae");
        return retrying(io(() -> restClient.get(url, OrganisaatioHakutulos.class)), 2)
                .get().orFail(mapper(url)).getOrganisaatiot();
    }

    @Override
    public List<OrganisaatioPerustieto> listActiveOganisaatioPerustiedotRecursiveCached(String organisaatioOid, Mode mode) {
        return cached(c -> c.flatWithParentsAndChildren(organisaatioOid)
                .filter(org -> !rootOrganizationOid.equals(org.getOid())) // the resource never returns the root
                .collect(toList()),
                () -> {
                    String url = urlConfiguration.url("organisaatio-service.organisaatio.hae");
                    String params = "?oid="+organisaatioOid + "&aktiiviset=true";
                    return retrying(io(() -> restClient.get(url+params,OrganisaatioHakutulos.class)), 2)
                            .get().orFail(mapper(url)).getOrganisaatiot();
                },
                mode);
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
        String url = urlConfiguration.url("organisaatio-service.organisaatio.parentOids", oid);
        return Stream.of(io(() -> restClient.getAsString(url)).get().split("/")).collect(toList());
    }

    @Override
    public List<String> getChildOids(String oid) {
        String url = urlConfiguration.url("organisaatio-service.organisaatio.childOids", oid);
        return cached(c -> c.flatWithChildrenByOid(oid)
                        .map(OrganisaatioPerustieto::getOid)
                        .collect(toList()),
                () -> retrying(io(() -> (MuutetutOidListContainer) objectMapper.readerFor(MuutetutOidListContainer.class)
                        .readValue(restClient.getAsString(url))), 2).get().orFail(mapper(url)).getOids(),
                Mode.requireCache());
    }

}
