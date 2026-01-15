package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckDto;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/s2s", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Service to Service")
public class ServiceToServiceController {

    private PermissionCheckerService permissionCheckerService;

    public ServiceToServiceController(PermissionCheckerService permissionCheckerService,
            OrganisaatioHenkiloService organisaatioHenkiloService) {
        this.permissionCheckerService = permissionCheckerService;
    }

    @Operation(summary = "Palauttaa tiedon, onko käyttäjällä oikeus toiseen käyttäjään")
    @PreAuthorize("hasAnyRole('APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @PostMapping(value = "/canUserAccessUser", consumes = MediaType.APPLICATION_JSON_VALUE)
    public boolean checkUserPermissionToUser(@RequestBody PermissionCheckDto permissionCheckDto) {
        return permissionCheckerService.isAllowedToAccessPerson(permissionCheckDto);
    }
}
