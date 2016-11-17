package fi.vm.sade.kayttooikeus.service.external.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.kayttooikeus.config.properties.UrlConfiguration;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static fi.vm.sade.kayttooikeus.service.external.ExternalServiceException.mapper;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.io;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.retrying;
import static java.util.stream.Collectors.joining;

public class OrganisaatioClientImpl implements OrganisaatioClient {
    private final CachingRestClient restClient = new CachingRestClient()
            .setClientSubSystemCode("kayttooikeus.kayttooikeuspalvelu-service");
    private final UrlConfiguration urlConfiguration;
    private final ObjectMapper objectMapper;

    public OrganisaatioClientImpl(UrlConfiguration urlConfiguration, ObjectMapper objectMapper) {
        this.urlConfiguration = urlConfiguration;
        this.objectMapper = objectMapper;
    }

    @Override
    public OrganisaatioRDTO getOrganisaatioPerustiedot(String oid) {
        String url = urlConfiguration.url("organisaatio-service.organisaatio.perustiedot", oid);
        return retrying(io(() -> (OrganisaatioRDTO) objectMapper.readerFor(OrganisaatioRDTO.class)
                    .readValue(restClient.getAsString(url))), 2).get().orFail(mapper(url));
    }

    @Override
    public List<OrganisaatioPerustieto> listOganisaatioPerustiedotRecusive(Collection<String> organisaatioOids) {
        return getOrganisaatioPerustietosRecursive(organisaatioOids, false);
    }

    @Override
    public List<OrganisaatioPerustieto> listActiveOganisaatioPerustiedotRecursive(Collection<String> organisaatioOids) {
        return getOrganisaatioPerustietosRecursive(organisaatioOids, true);
    }

    private List<OrganisaatioPerustieto> getOrganisaatioPerustietosRecursive(Collection<String> organisaatioOids, boolean limitToActive) {
        if (organisaatioOids.isEmpty()) {
            return new ArrayList<>();
        }
        String url = urlConfiguration.url("organisaatio-service.organisaatio.hae");
        String params = "?oidRestrictionList="+organisaatioOids.stream().collect(joining("&oidRestrictionList=")) +
                (limitToActive ? "&aktiiviset=true" : "");
        return retrying(io(() -> restClient.get(url+params,OrganisaatioHakutulos.class)), 2)
                .get().orFail(mapper(url)).getOrganisaatiot();
    }

}
