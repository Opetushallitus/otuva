package fi.vm.sade.kayttooikeus.service.external.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.config.properties.UrlConfiguration;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.ExternalServiceException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioHakutulos;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
    private DateTime cacheUpdatedAt;
    private LocalDate latestChanges;
    private OrganisaatioCache cache;
    
    
    public OrganisaatioClientImpl(UrlConfiguration urlConfiguration, ObjectMapper objectMapper, CommonProperties commonProperties) {
        this.urlConfiguration = urlConfiguration;
        this.objectMapper = objectMapper;
        this.rootOrganizationOid = commonProperties.getRootOrganizationOid();
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

    private synchronized void refreshCache(DateTime updateMoment) {
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
            cacheUpdatedAt = DateTime.now();
        }
    }
    
    @Getter @Setter
    public static class MuutetutOidListContainer {
        private List<String> oids;
    }

    private boolean changesSince(LocalDate date) {
        String url = urlConfiguration.url("organisaatio-service.organisaatio.muutetut.oid", date.toString());
        boolean result = !isReallyEmpty(retrying(io(() -> (MuutetutOidListContainer) objectMapper.readerFor(MuutetutOidListContainer.class)
                .readValue(restClient.getAsString(url))), 2).get().orFail(mapper(url)).getOids());
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
    public OrganisaatioPerustieto getOrganisaatioPerustiedotCached(String oid, Mode mode) {
        return cached(c -> c.getByOid(oid).<NotFoundException>orElseThrow(() -> new NotFoundException("Organization not found by oid " + oid)),
                () -> fetchPerustiedot(oid), mode);
    }

    public OrganisaatioPerustieto fetchPerustiedot(String oid) {
        String url = urlConfiguration.url("organisaatio-service.organisaatio.perustiedot", oid);
        return mapToPerustieto(retrying(io(() -> (OrganisaatioRDTO) objectMapper.readerFor(OrganisaatioRDTO.class)
                    .readValue(restClient.getAsString(url))), 2).get().orFail(mapper(url)));
    }

    private OrganisaatioPerustieto mapToPerustieto(OrganisaatioRDTO organisaatioRDTO) {
        OrganisaatioPerustieto perustieto = new OrganisaatioPerustieto();
        perustieto.setOid(organisaatioRDTO.getOid());
        perustieto.setParentOid(organisaatioRDTO.getParentOid());
        perustieto.setParentOidPath(organisaatioRDTO.getParentOidPath());
        perustieto.setNimi(organisaatioRDTO.getNimi());
        perustieto.setOrganisaatiotyypit(organisaatioRDTO.getTyypit());
        perustieto.setOppilaitosKoodi(organisaatioRDTO.getOppilaitosKoodi());
        perustieto.setOppilaitostyyppi(organisaatioRDTO.getOppilaitosTyyppiUri());
        perustieto.setKieletUris(organisaatioRDTO.getKieletUris());
        perustieto.setVirastotunnus(organisaatioRDTO.getVirastoTunnus());
        perustieto.setYtunnus(organisaatioRDTO.getYTunnus());
        perustieto.setKotipaikkaUri(organisaatioRDTO.getKotipaikkaUri());
        perustieto.setAlkuPvm(organisaatioRDTO.getAlkuPvm());
        perustieto.setLakkautusPvm(organisaatioRDTO.getLakkautusPvm());
        perustieto.setChildren(new ArrayList<>());
        return perustieto;
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
                .collect(toList()), () -> {
            String url = urlConfiguration.url("organisaatio-service.organisaatio.hae");
            String params = "?oid="+organisaatioOid + "&aktiiviset=true";
            return retrying(io(() -> restClient.get(url+params,OrganisaatioHakutulos.class)), 2)
                    .get().orFail(mapper(url)).getOrganisaatiot();
        }, mode);
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

}
