package fi.vm.sade.kayttooikeus.service.external;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrganisaatioClient {
    List<String> getChildOids(String oid);

    List<OrganisaatioPerustieto> listActiveOganisaatioPerustiedotRecursiveCached(String organisaatioOid);

    List<OrganisaatioPerustieto> refreshCache();

    Optional<OrganisaatioPerustieto> getOrganisaatioPerustiedotCached(String oid);

    List<OrganisaatioPerustieto> listActiveOrganisaatioPerustiedotByOidRestrictionList(Collection<String> organisaatioOids);

    List<String> getParentOids(String oid);
}
