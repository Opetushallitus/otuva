package fi.vm.sade.kayttooikeus.controller;


import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloListDto;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/henkilo")
@Api(value = "/organisaatiohenkilo", description = "Henkilön organisaatiohenkilöihin liittyvät operaatiot.")
public class HenkiloController {
    private final OrganisaatioHenkiloService organisaatioHenkiloService;
    
    @Autowired
    public HenkiloController(OrganisaatioHenkiloService organisaatioHenkiloService) {
        this.organisaatioHenkiloService = organisaatioHenkiloService;
    }
    
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_READ_UPDATE',"
            + "'ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @RequestMapping(value = "/{oid}/organisaatio", method = RequestMethod.GET)
    public List<OrganisaatioHenkiloListDto> listOrganisatioHenkilos(
            @PathVariable @ApiParam("Henkilö-OID") String oid,
            @RequestParam(required = false, defaultValue = "fi") @ApiParam("Organisaatioiden järjestyksen kielikoodi (oletus fi)") String comparisonLangCode) {
        return organisaatioHenkiloService.listOrganisaatioHenkilos(oid, comparisonLangCode);
    }
}
