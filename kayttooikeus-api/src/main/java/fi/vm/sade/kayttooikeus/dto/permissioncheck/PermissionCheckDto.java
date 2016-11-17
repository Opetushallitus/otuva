package fi.vm.sade.kayttooikeus.dto.permissioncheck;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter @Setter
public class PermissionCheckDto {
    String callingUserOid;

    String userOid;

    List<String> allowedRoles;

    ExternalPermissionService externalPermissionService;

    Set<String> callingUserRoles;
}
