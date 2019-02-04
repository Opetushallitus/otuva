package fi.vm.sade.kayttooikeus.dto.permissioncheck;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter @Setter
public class PermissionCheckDto {
    String callingUserOid;

    String userOid;

    Map<String, List<String>> allowedPalveluRooli;

    ExternalPermissionService externalPermissionService;

    Set<String> callingUserRoles;

    /**
     * Organisaatiot joihin kutsuja on löytänyt hyväksymiään käyttöoikeuksia. Vain näitä organisaatioita kysytään
     * ulkopuolisilta palveluilta. Voi olla null.
     */
    Set<String> permittedOrganisations;
}
