package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloCreateDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloDto;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckDto;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/s2s", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Service to Service")
public class ServiceToServiceController {

    private PermissionCheckerService permissionCheckerService;
    private OrganisaatioHenkiloService organisaatioHenkiloService;

    public ServiceToServiceController(PermissionCheckerService permissionCheckerService,
            OrganisaatioHenkiloService organisaatioHenkiloService) {
        this.permissionCheckerService = permissionCheckerService;
        this.organisaatioHenkiloService = organisaatioHenkiloService;
    }

    @Operation(summary = "Palauttaa tiedon, onko käyttäjällä oikeus toiseen käyttäjään")
    @PreAuthorize("hasAnyRole('APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @PostMapping(value = "/canUserAccessUser", consumes = MediaType.APPLICATION_JSON_VALUE)
    public boolean checkUserPermissionToUser(@RequestBody PermissionCheckDto permissionCheckDto) {
        return permissionCheckerService.isAllowedToAccessPerson(permissionCheckDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Lisää henkilölle organisaatiot.",
            description = "Lisää uudet organisaatiot henkilölle. Ei päivitä tai poista vanhoja organisaatiotietoja. Palauttaa henkilön kaikki nykyiset organisaatiot.")
    @PostMapping(value = "/henkilo/{oid}/organisaatio/findOrCreate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<OrganisaatioHenkiloDto> addOrganisaatioHenkilot(@PathVariable("oid") String henkiloOid,
            @RequestBody @Validated List<OrganisaatioHenkiloCreateDto> organisaatioHenkilot) {
        return organisaatioHenkiloService.addOrganisaatioHenkilot(henkiloOid, organisaatioHenkilot);
    }
}
