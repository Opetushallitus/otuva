package fi.vm.sade.kayttooikeus.service.external;

import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;

import java.util.Collection;
import java.util.List;

public interface OrganisaatioClient {
    List<OrganisaatioPerustieto> listOganisaatioPerustiedot(Collection<String> organisaatioOids);

    List<OrganisaatioPerustieto> listActiveOganisaatioPerustiedot(Collection<String> organisaatioOids);

    OrganisaatioRDTO getOrganisaatioPerustiedot(String oid);
}
