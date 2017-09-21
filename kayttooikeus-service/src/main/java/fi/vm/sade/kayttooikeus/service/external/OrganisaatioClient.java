package fi.vm.sade.kayttooikeus.service.external;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrganisaatioClient {
    List<String> getChildOids(String organisaatioOid);

    void throwIfActiveNotFound(String organisaatioOid);

    List<OrganisaatioPerustieto> listActiveOganisaatioPerustiedotRecursiveCached(String organisaatioOid);

    List<OrganisaatioPerustieto> refreshCache();

    Optional<OrganisaatioPerustieto> getOrganisaatioPerustiedotCached(String organisaatioOid);

    List<OrganisaatioPerustieto> listActiveOrganisaatioPerustiedotByOidRestrictionList(Collection<String> organisaatioOids);

    List<String> getParentOids(String organisaatioOid);

    List<String> getActiveParentOids(String organisaatioOid);

    List<String> getActiveChildOids(String organisaatioOid);
}
