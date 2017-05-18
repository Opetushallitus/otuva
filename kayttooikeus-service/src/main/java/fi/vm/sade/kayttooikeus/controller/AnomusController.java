package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.model.Anomus;
import fi.vm.sade.kayttooikeus.service.KayttooikeusAnomusService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Kayttooikeusanomukset")
@RestController
@RequestMapping("/kayttooikeusanomus")
public class AnomusController {

    private final KayttooikeusAnomusService kayttooikeusAnomusService;

    @Autowired
    AnomusController(KayttooikeusAnomusService kayttooikeusAnomusService) {
        this.kayttooikeusAnomusService = kayttooikeusAnomusService;
    }

    @ApiOperation("Palauttaa henkilön kaikki haetut käyttöoikeusryhmät")
    @PostAuthorize("@permissionCheckerServiceImpl.hasRoleForOrganisations(returnObject, {'READ', 'READ_UPDATE', 'CRUD'})")
    @RequestMapping(value = "/{oidHenkilo}", method = RequestMethod.GET)
    public List<HaettuKayttooikeusryhmaDto> getActiveAnomuksetByHenkilo(@ApiParam("Henkilön OID") @PathVariable String oidHenkilo,
                                                                        @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        return this.kayttooikeusAnomusService.getAllActiveAnomusByHenkiloOid(oidHenkilo, activeOnly);
    }

    @ApiOperation("Tekee uuden käyttöoikeusanomuksen")
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/{anojaOid}", method = RequestMethod.POST)
    public Long createKayttooikeusAnomus(@ApiParam("Anojan OID") @PathVariable String anojaOid,
                                           @RequestBody @Validated KayttooikeusAnomusDto kayttooikeusAnomusDto) {
        return this.kayttooikeusAnomusService.createKayttooikeusAnomus(anojaOid, kayttooikeusAnomusDto);
    }


    @ApiOperation("Hyväksyy tai hylkää haetun käyttöoikeusryhmän")
    // Organisation access validated on server layer
    @PreAuthorize("hasAnyRole('ROLE_APP_ANOMUSTENHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void updateHaettuKayttooikeusryhma(@ApiParam("kayttoOikeudenTila MYONNETTY tai HYLATTY")
                                                  @RequestBody @Validated UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto) {
        this.kayttooikeusAnomusService.updateHaettuKayttooikeusryhma(updateHaettuKayttooikeusryhmaDto);
    }

    @ApiOperation("Myöntää halutut käyttöoikeusryhmät käyttäjälle haluttuun organisaatioon")
    // Organisation access validated on server layer
    @PreAuthorize("hasAnyRole('ROLE_APP_ANOMUSTENHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @RequestMapping(value = "/{oidHenkilo}/{organisaatioOid}", method = RequestMethod.PUT)
    public void grantMyonnettyKayttooikeusryhmaForHenkilo(@PathVariable String oidHenkilo, @PathVariable String organisaatioOid,
                                                          @RequestBody @Validated List<GrantKayttooikeusryhmaDto>
                                                                  grantKayttooikeusryhmaDtoList) {
        this.kayttooikeusAnomusService.grantKayttooikeusryhma(oidHenkilo, organisaatioOid, grantKayttooikeusryhmaDtoList);
    }

    @ApiOperation("Poistaa haetun käyttöoikeusryhmän käyttäjän omalta käyttöoikeusanomukselta")
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/peruminen/currentuser", method = RequestMethod.PUT)
    public void cancelKayttooikeusRyhmaAnomus(@RequestBody @Validated Long kayttooikeusRyhmaId) {
        this.kayttooikeusAnomusService.cancelKayttooikeusAnomus(kayttooikeusRyhmaId);
    }

}
