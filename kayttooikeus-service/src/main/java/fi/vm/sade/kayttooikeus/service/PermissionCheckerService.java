package fi.vm.sade.kayttooikeus.service;


import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;

import java.util.List;
import java.util.Set;

public interface PermissionCheckerService {
    boolean isAllowedToAccessPerson(String personOid, List<String> allowedRoles, ExternalPermissionService permissionService);
    boolean isAllowedToAccessPerson(String callingUserOid, String personOid, List<String> allowedRoles,
                                    ExternalPermissionService permissionCheckService, Set<String> callingUserRoles);
    List<OrganisaatioPerustieto> listOrganisaatiosByHenkiloOid(String oid);
    boolean hasInternalAccess(String personOid, List<String> allowedRolesWithoutPrefix, Set<String> callingUserRoles);
}
