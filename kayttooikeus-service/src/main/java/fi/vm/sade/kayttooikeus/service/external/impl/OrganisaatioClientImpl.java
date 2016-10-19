package fi.vm.sade.kayttooikeus.service.external.impl;

import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.kayttooikeus.config.properties.UrlConfiguration;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static fi.vm.sade.kayttooikeus.service.external.ExternalServiceException.mapper;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.io;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.retrying;
import static java.util.stream.Collectors.joining;

/**
 * User: tommiratamaa
 * Date: 12/10/2016
 * Time: 15.05
 */
public class OrganisaatioClientImpl implements OrganisaatioClient {
    private final CachingRestClient restClient = new CachingRestClient()
            .setClientSubSystemCode("kayttooikeus.kayttooikeuspalvelu-service");
    private final UrlConfiguration urlConfiguration;

    public OrganisaatioClientImpl(UrlConfiguration urlConfiguration) {
        this.urlConfiguration = urlConfiguration;
    }

    @Override
    public OrganisaatioRDTO getOrganisaatioPerustiedot(String oid) {
        String url = urlConfiguration.getProperty("organisaatio-service.organisaatio.perustiedot", oid);
        return retrying(io(() -> restClient.get(url,OrganisaatioRDTO.class)), 2)
                .get().orFail(mapper(url));
    }

    @Override
    public List<OrganisaatioPerustieto> listOganisaatioPerustiedot(Collection<String> organisaatioOids) {
        return getOrganisaatioPerustietos(organisaatioOids, false);
    }

    @Override
    public List<OrganisaatioPerustieto> listActiveOganisaatioPerustiedot(Collection<String> organisaatioOids) {
        return getOrganisaatioPerustietos(organisaatioOids, true);
    }

    private List<OrganisaatioPerustieto> getOrganisaatioPerustietos(Collection<String> organisaatioOids, boolean limitToActive) {
        if (organisaatioOids.isEmpty()) {
            return new ArrayList<>();
        }
        String url = urlConfiguration.getProperty("organisaatio-service.organisaatio.hae");
        String params = "?oidRestrictionList="+organisaatioOids.stream().collect(joining("&oidRestrictionList=")) +
                (limitToActive ? "&aktiiviset=true" : "");
        return retrying(io(() -> restClient.get(url+params,OrganisaatioHakutulos.class)), 2)
                .get().orFail(mapper(url)).getOrganisaatiot();
    }
}
