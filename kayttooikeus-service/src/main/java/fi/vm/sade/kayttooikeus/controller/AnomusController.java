package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.GrantKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.dto.HaettuKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.dto.KayttooikeusAnomusDto;
import fi.vm.sade.kayttooikeus.dto.UpdateHaettuKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.kayttooikeus.enumeration.OrderByAnomus;
import fi.vm.sade.kayttooikeus.repositories.criteria.AnomusCriteria;
import fi.vm.sade.kayttooikeus.service.KayttooikeusAnomusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "Käyttöoikeusanomukset ja käyttöoikeuksien hallinta")
@RestController
@RequestMapping(value = "/kayttooikeusanomus", produces = MediaType.APPLICATION_JSON_VALUE)
public class AnomusController {

    private final KayttooikeusAnomusService kayttooikeusAnomusService;

    AnomusController(KayttooikeusAnomusService kayttooikeusAnomusService) {
        this.kayttooikeusAnomusService = kayttooikeusAnomusService;
    }

    @GetMapping("/haettuKayttoOikeusRyhma")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Hakee haetut käyttöoikeusryhmät, jotka käyttäjän on oikeus hyväksyä omien käyttöoikeusryhmiensä kautta")
    public List<HaettuKayttooikeusryhmaDto> listHaetutKayttoOikeusRyhmat(AnomusCriteria criteria,
            @RequestParam(required = false, defaultValue = "20") Long limit,
            @RequestParam(required = false) Long offset,
            @RequestParam(required = false) OrderByAnomus orderBy) {
        return this.kayttooikeusAnomusService.listHaetutKayttoOikeusRyhmat(criteria, limit, offset, orderBy);
    }

    @Operation(summary = "Palauttaa henkilön kaikki haetut käyttöoikeusryhmät")
    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPersonOrSelf(#oidHenkilo, {'KAYTTOOIKEUS': {'READ', 'CRUD', 'PALVELUKAYTTAJA_CRUD'}}, #permissionService)")
    @RequestMapping(value = "/{oidHenkilo}", method = RequestMethod.GET)
    public List<HaettuKayttooikeusryhmaDto> getActiveAnomuksetByHenkilo(
            @Parameter(description = "Henkilön OID") @PathVariable String oidHenkilo,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @RequestHeader(value = "External-Permission-Service", required = false) ExternalPermissionService permissionService) {
        return this.kayttooikeusAnomusService.listHaetutKayttoOikeusRyhmat(oidHenkilo, activeOnly);
    }

    @Operation(summary = "Tekee uuden käyttöoikeusanomuksen")
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/{anojaOid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Long createKayttooikeusAnomus(@Parameter(description = "Anojan OID") @PathVariable String anojaOid,
                                         @RequestBody @Validated KayttooikeusAnomusDto kayttooikeusAnomusDto) {
        return this.kayttooikeusAnomusService.createKayttooikeusAnomus(anojaOid, kayttooikeusAnomusDto);
    }


    @Operation(summary = "Hyväksyy tai hylkää haetun käyttöoikeusryhmän")
    // Organisation access validated on service layer
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @PutMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateHaettuKayttooikeusryhma(@Parameter(description = "kayttoOikeudenTila MYONNETTY tai HYLATTY")
                                                  @RequestBody @Validated UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto) {
        this.kayttooikeusAnomusService.updateHaettuKayttooikeusryhma(updateHaettuKayttooikeusryhmaDto);
    }

    @Operation(summary = "Myöntää halutut käyttöoikeusryhmät käyttäjälle haluttuun organisaatioon")
    // Organisation access validated on service layer
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @PutMapping(value = "/{oidHenkilo}/{organisaatioOid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void grantMyonnettyKayttooikeusryhmaForHenkilo(@PathVariable String oidHenkilo, @PathVariable String organisaatioOid,
                                                          @RequestBody @Validated List<GrantKayttooikeusryhmaDto>
                                                                  grantKayttooikeusryhmaDtoList) {
        this.kayttooikeusAnomusService.grantKayttooikeusryhma(oidHenkilo, organisaatioOid, grantKayttooikeusryhmaDtoList);
    }

    @Operation(summary = "Poistaa haetun käyttöoikeusryhmän käyttäjän omalta käyttöoikeusanomukselta")
    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/peruminen/currentuser", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void cancelKayttooikeusRyhmaAnomus(@RequestBody @Validated Long kayttooikeusRyhmaId) {
        this.kayttooikeusAnomusService.cancelKayttooikeusAnomus(kayttooikeusRyhmaId);
    }

    @PostMapping(value = "/ilmoitus", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lähettää käyttöoikeusanomuksista sähköposti-ilmoituksen anomuksien hyväksyjille")
    public void lahetaUusienAnomuksienIlmoitukset(
            @RequestParam
            @Parameter(description = "yyyy-MM-dd")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anottuPvm) {
        this.kayttooikeusAnomusService.lahetaUusienAnomuksienIlmoitukset(anottuPvm);
    }

    @Operation(summary = "Poistaa henkilöltä käyttöoikeuden halutusta organisaatiosta")
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @RequestMapping(value = "/{oidHenkilo}/{organisaatioOid}/{id}", method = RequestMethod.DELETE)
    public void removePrivilege(@PathVariable String oidHenkilo,
                                @PathVariable String organisaatioOid,
                                @Parameter(description = "Käyttöoikeusryhmä id", required = true) @PathVariable Long id) {
        this.kayttooikeusAnomusService.removePrivilege(oidHenkilo, id, organisaatioOid);
    }

    @Operation(summary = "Listaa organisaatioittain ne käyttöoikeusryhmät, joita käyttäjällä on oikeus myöntää kyseiselle henkilölle",
            description = "Ei sisällä kaikkia mahdollisia ryhmiä vaan vain henkilön anomukset, jo olemassa olevat käyttöoikeudet ja " +
                    "joskus voimassa olleet käyttöoikeudet.")
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/henkilo/current/{henkiloOid}/canGrant", method = RequestMethod.GET)
    public Map<String, Set<Long>> currentHenkiloCanGrant(@PathVariable String henkiloOid) {
        return this.kayttooikeusAnomusService.findCurrentHenkiloCanGrant(henkiloOid);
    }

}
