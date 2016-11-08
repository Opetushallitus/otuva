package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckDto;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/s2s")
@Api(tags = "Service to Service")
public class ServiceToServiceController {

    private PermissionCheckerService permissionCheckerService;

    @Autowired
    public ServiceToServiceController(PermissionCheckerService permissionCheckerService) {
        this.permissionCheckerService = permissionCheckerService;
    }

    @ApiOperation("Palauttaa tiedon, onko käyttäjällä oikeus toiseen käyttäjään")
    @PreAuthorize("hasRole('APP_HENKILONHALLINTA_OPHREKISTERI')")
    @RequestMapping(value = "/canUserAccessUser", method = RequestMethod.POST)
    public boolean checkUserPermissionToUser(@RequestBody PermissionCheckDto permissionCheckDto) {
        return permissionCheckerService.isAllowedToAccessPerson(permissionCheckDto.getCallingUserOid(),
                permissionCheckDto.getUserOid(), permissionCheckDto.getAllowedRoles(),
                permissionCheckDto.getExternalPermissionService(), permissionCheckDto.getCallingUserRoles());
    }
}

