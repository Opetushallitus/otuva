package fi.vm.sade.kayttooikeus.service;


import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.kayttooikeus.model.OrganisaatioViite;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface PermissionCheckerService {
    boolean isAllowedToAccessPerson(String personOid, List<String> allowedRoles, ExternalPermissionService permissionService);

    boolean isAllowedToAccessPersonOrSelf(String personOid, List<String> allowedRoles, ExternalPermissionService permissionService);

    boolean isAllowedToAccessPerson(String callingUserOid, String personOid, List<String> allowedRoles,
                                    ExternalPermissionService permissionCheckService, Set<String> callingUserRoles);

    boolean checkRoleForOrganisation(List<String> orgOidList, List<String> allowedRolesWithoutPrefix);

    List<OrganisaatioPerustieto> listOrganisaatiosByHenkiloOid(String oid);

    boolean hasInternalAccess(String personOid, List<String> allowedRolesWithoutPrefix, Set<String> callingUserRoles);

    boolean hasRoleForOrganisations(List<Object> organisaatioHenkiloDtoList, List<String> allowedRolesWithoutPrefix);

    boolean hasRoleForOrganization(String orgOid, List<String> allowedRolesWithoutPrefix, Set<String> callingUserRoles);

    boolean notOwnData(String dataOwnderOid);

    String getCurrentUserOid();

    Set<String> getCasRoles();

    /**
     * Rekisterinpitäjä
     * @return isRekisterinpitäjä
     */
    boolean isCurrentUserAdmin();

    /**
     * OPH-virkailija
     * @return isOph-virkailij
     */
    boolean isCurrentUserMiniAdmin();

    boolean hasOrganisaatioInHierarcy(String requiredOrganiaatioOid);

    Set<String> hasOrganisaatioInHierarcy(Collection<String> requiredOrganiaatioOid);

    boolean kayttooikeusMyontoviiteLimitationCheck(Long kayttooikeusryhmaId);

    boolean organisaatioLimitationCheck(String organisaatioOid, Set<OrganisaatioViite> viiteSet);
}
