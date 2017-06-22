package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.model.AnomuksenTila;
import fi.vm.sade.kayttooikeus.repositories.criteria.AnomusCriteria;
import fi.vm.sade.kayttooikeus.service.KayttooikeusAnomusService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

@Api(tags = "Kayttooikeusanomukset")
@RestController
@RequestMapping("/kayttooikeusanomus")
public class AnomusController {

    private final KayttooikeusAnomusService kayttooikeusAnomusService;

    @Autowired
    AnomusController(KayttooikeusAnomusService kayttooikeusAnomusService) {
        this.kayttooikeusAnomusService = kayttooikeusAnomusService;
    }

    @GetMapping("/haettuKayttoOikeusRyhma")
    @PreAuthorize("hasRole('ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation(value = "Hakee haetut käyttöoikeusryhmät",
            notes = "Tällä hetkellä toteutus vain rekisterinpitäjälle")
    public List<HaettuKayttooikeusryhmaDto> listHaetutKayttoOikeusRyhmat(AnomusCriteria criteria,
            @RequestParam(required = false, defaultValue = "20") Long limit,
            @RequestParam(required = false) Long offset) {
        return kayttooikeusAnomusService.listHaetutKayttoOikeusRyhmat(criteria, limit, offset);
    }

    @ApiOperation("Palauttaa henkilön kaikki haetut käyttöoikeusryhmät")
    @PostAuthorize("@permissionCheckerServiceImpl.hasRoleForOrganisations(returnObject, {'READ', 'READ_UPDATE', 'CRUD'})")
    @RequestMapping(value = "/{oidHenkilo}", method = RequestMethod.GET)
    public List<HaettuKayttooikeusryhmaDto> getActiveAnomuksetByHenkilo(@ApiParam("Henkilön OID") @PathVariable String oidHenkilo,
                                                                        @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        AnomusCriteria criteria = new AnomusCriteria();
        criteria.setAnojaOid(oidHenkilo);
        if (activeOnly) {
            criteria.setTilat(EnumSet.of(AnomuksenTila.ANOTTU));
        }
        return kayttooikeusAnomusService.listHaetutKayttoOikeusRyhmat(criteria, null, null);
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

    @PostMapping("/ilmoitus")
    @ApiOperation(value = "Lähettää käyttöoikeusanomuksista sähköposti-ilmoituksen anomuksien hyväksyjille")
    public void lahetaUusienAnomuksienIlmoitukset(
            @RequestParam
            @ApiParam("yyyy-MM-dd")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anottuPvm) {
        this.kayttooikeusAnomusService.lahetaUusienAnomuksienIlmoitukset(anottuPvm);
    }

    @ApiOperation("Poistaa henkilöltä käyttöoikeuden halutusta organisaatiosta")
    @PreAuthorize("hasAnyRole('ROLE_APP_ANOMUSTENHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @RequestMapping(value = "/{oidHenkilo}/{organisaatioOid}/{id}", method = RequestMethod.DELETE)
    public void removePrivilege(@PathVariable String oidHenkilo,
                                @PathVariable String organisaatioOid,
                                @ApiParam(value = "Käyttöoikeusryhmä id", required = true) @PathVariable Long id) {
        this.kayttooikeusAnomusService.removePrivilege(oidHenkilo, id, organisaatioOid);
    }

    @ApiOperation("Listaa organisaatioittain ne ryhmät, joita käyttäjällä on oikeus myöntää kyseiselle henkilölle")
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/henkilo/current/{henkiloOid}/canGrant", method = RequestMethod.GET)
    public Map<String, Set<Long>> currentHenkiloCanGrant(@PathVariable String henkiloOid) {
        return this.kayttooikeusAnomusService.findCurrentHenkiloCanGrant(henkiloOid);
    }

}
