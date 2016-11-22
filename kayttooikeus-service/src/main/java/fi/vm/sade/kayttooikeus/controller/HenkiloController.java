package fi.vm.sade.kayttooikeus.controller;


import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloWithOrganisaatioDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioOidsSearchDto;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/henkilo")
@Api(value = "/organisaatiohenkilo", description = "Henkilön organisaatiohenkilöihin liittyvät operaatiot.")
public class HenkiloController {
    private final OrganisaatioHenkiloService organisaatioHenkiloService;
    private final HenkiloService henkiloService;

    @Autowired
    public HenkiloController(OrganisaatioHenkiloService organisaatioHenkiloService,
                             HenkiloService henkiloService) {
        this.organisaatioHenkiloService = organisaatioHenkiloService;
        this.henkiloService = henkiloService;
    }

    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPersonOrSelf(#oid, {'READ', 'READ_UPDATE', 'CRUD'}, #permissionService)")
    @ApiOperation(value = "Listaa henkilön aktiiviset organisaatiot organisaatioiden tiedoilla.",
            notes = "Hakee annetun henkilön aktiiviset organisaatiohenkilöt organisaation tiedoilla.")
    @RequestMapping(value = "/{oid}/organisaatio", method = RequestMethod.GET)
    public List<OrganisaatioHenkiloWithOrganisaatioDto> listOrganisatioHenkilos(
            @PathVariable @ApiParam("Henkilö-OID") String oid,
            @RequestParam(required = false, defaultValue = "fi") @ApiParam("Organisaatioiden järjestyksen kielikoodi (oletus fi)") String comparisonLangCode,
            @RequestParam(value = "permissionService", required = false) ExternalPermissionService permissionService) {
        return organisaatioHenkiloService.listOrganisaatioHenkilos(oid, comparisonLangCode);
    }
    
    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPerson(#henkiloOid, {'READ', 'READ_UPDATE', 'CRUD'}, #permissionService)")
    @ApiOperation(value = "Listaa henkilön organisaatiot.",
            notes = "Hakee annetun henkilön kaikki organisaatiohenkilöt.")
    @RequestMapping(value = "/{oid}/organisaatiohenkilo", method = RequestMethod.GET)
    public List<OrganisaatioHenkiloDto> listOrganisaatioHenkilos(@PathVariable("oid") String henkiloOid,
                                                                 @RequestParam(value = "permissionService", required = false) ExternalPermissionService permissionService) {
        return organisaatioHenkiloService.findOrganisaatioByHenkilo(henkiloOid);
    }

    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPerson(#henkiloOid, {'READ', 'READ_UPDATE', 'CRUD'}, null)")
    @ApiOperation(value = "Hakee henkilön yhden organisaation tiedot.",
            notes = "Hakee henkilön yhden organisaatiohenkilön tiedot.")
    @RequestMapping(value = "/{oid}/organisaatiohenkilo/{organisaatioOid}", method = RequestMethod.GET)
    public OrganisaatioHenkiloDto findByOrganisaatioOid(@PathVariable("oid") String henkiloOid,
                                                        @PathVariable("organisaatioOid") String organisaatioOid) {
        return organisaatioHenkiloService.findOrganisaatioHenkiloByHenkiloAndOrganisaatio(henkiloOid, organisaatioOid);
    }

    @ApiOperation(value = "Hakee henkilöitä organisaatioiden ja käyttöoikeuksien mukaan.",
            notes = "Tämä toteutus on tehty Osoitepalvelua varten, jonka pitää pystyä "
                    + "hakemaan henkilöitä henkilötyyppien, organisaatioiden sekä"
                    + "käyttöoikeusryhmien mukaan, tämä toteutus ei ole UI:n käytössä.")
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_READ',"
            + "'ROLE_APP_HENKILONHALLINTA_READ_UPDATE',"
            + "'ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @RequestMapping(value = "/byOoids", method = RequestMethod.POST)
    public List<String> findHenkilosByOrganisaatioOids(@RequestBody @Valid OrganisaatioOidsSearchDto organisaatioOidsSearchDto) {
        return henkiloService.findHenkilos(organisaatioOidsSearchDto);
    }
}
