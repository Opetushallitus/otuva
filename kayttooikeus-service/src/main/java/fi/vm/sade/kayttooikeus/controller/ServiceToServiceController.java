package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

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
    public boolean checkUserPermissionToUser(@RequestParam("callinguseroid") String callingUserOid,
                                             @RequestParam("useroid") String userOid,
                                             @RequestParam("allowedroles") List<String> allowedRoles,
                                             @RequestParam(value = "externalpermissionservice", required = false)
                                                     ExternalPermissionService externalPermissionService,
                                             @RequestParam("callinguserroles") Set<String> callingUserRoles) {
        return permissionCheckerService.isAllowedToAccessPerson(callingUserOid, userOid, allowedRoles,
                externalPermissionService, callingUserRoles);
    }
}

