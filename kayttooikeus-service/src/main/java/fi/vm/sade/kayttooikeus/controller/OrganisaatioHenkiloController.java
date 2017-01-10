package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloCreateDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloDto;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organisaatiohenkilo")
@Api(value = "/organisaatiohenkilo", description = "Organisaatiohenkilön käsittelyyn liittyvät operaatiot.")
public class OrganisaatioHenkiloController {
    private OrganisaatioHenkiloService organisaatioHenkiloService;
    
    @Autowired
    public OrganisaatioHenkiloController(OrganisaatioHenkiloService organisaatioHenkiloService) {
        this.organisaatioHenkiloService = organisaatioHenkiloService;
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_READ_UPDATE',"
            + "'ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @RequestMapping(value = "/current/organisaatio", method = RequestMethod.GET)
    @ApiOperation(value = "Hakee kirjautuneen käyttäjän organisaatioiden tiedot.",
            notes = "Tekee Organisaatiopalveluun haun nykyisen kirjautuneen "
                    + "käyttäjän organisaatioista ja palauttaa näiden organisaatioiden "
                    + "perustiedot hakuobjektin avulla.")
    public List<OrganisaatioPerustieto> listOrganisaatiosByCurrentHenkilo() {
        return organisaatioHenkiloService.listOrganisaatioPerustiedotForCurrentUser();
    }
    
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_READ',"
            + "'ROLE_APP_HENKILONHALLINTA_READ_UPDATE',"
            + "'ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @RequestMapping(value = "/current/availablehenkilotype", method = RequestMethod.GET)
    @ApiOperation(value = "Listaa sallitut organisaatiohenkilötyypit henkilöiden luontiin liittyen.",
            notes = "Listaa ne organisaatiohenkilötyypit joita kirjautunt käyttäjä saa luoda henkilöhallintaan.")
    public List<HenkiloTyyppi> listPossibleHenkiloTypesByCurrentHenkilo() {
        return organisaatioHenkiloService.listPossibleHenkiloTypesAccessibleForCurrentUser();
    }

    @PreAuthorize("@permissionCheckerServiceImpl.hasRoleForOrganizations(#organisaatioHenkiloList, {'CRUD'})")
    @RequestMapping(value = "/{oid}/findOrCreate", method = RequestMethod.POST)
    public List<OrganisaatioHenkiloDto> updateOrganisaatioHenkilos(@PathVariable(value = "oid") String oidHenkilo,
                                                                   @RequestBody List<OrganisaatioHenkiloCreateDto> organisaatioHenkiloList) {
        return this.organisaatioHenkiloService.addOrganisaatioHenkilot(oidHenkilo, organisaatioHenkiloList);
    }
}
