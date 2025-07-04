package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusService;
import fi.vm.sade.kayttooikeus.util.UserDetailsUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/kayttooikeusryhma", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "/kayttooikeusryhma", description = "Käyttöoikeusryhmien käsittelyyn liittyvät operaatiot.")
public class KayttoOikeusRyhmaController {
    private KayttoOikeusService kayttoOikeusService;

    public KayttoOikeusRyhmaController(KayttoOikeusService kayttoOikeusService) {
        this.kayttoOikeusService = kayttoOikeusService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listaa kaikki käyttöoikeusryhmät.",
            description = "Listaa kaikki käyttöoikeusryhmät, jotka on tallennettu henkilöhallintaan. "
                + "Suodattaa käyttöoikeusryhmistä vain ne, jotka sisältävät palvelun ja käyttöoikeuden, jos molemmat parametrit annetaan.")
    public List<KayttoOikeusRyhmaDto> listKayttoOikeusRyhma(
                @RequestParam(required = false) Boolean passiiviset,
                @RequestParam(required = false) String palvelu,
                @RequestParam(required = false) String kayttooikeus) {
        return kayttoOikeusService.listAllKayttoOikeusRyhmas(passiiviset, palvelu, kayttooikeus);
    }

    @Operation(summary = "Hakee henkilön käyttöoikeusryhmät organisaatioittain")
    @PreAuthorize("hasAnyRole('APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @RequestMapping(value = "ryhmasByOrganisaatio/{oid}", method = RequestMethod.GET)
    public Map<String, List<Integer>> ryhmasByOrganisation(@PathVariable("oid") String henkiloOid) {
        return this.kayttoOikeusService.findKayttooikeusryhmatAndOrganisaatioByHenkiloOid(henkiloOid);
    }

    @RequestMapping(value = "/organisaatio/{organisaatioOid}", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listaa käyttöoikeusryhmät organisaation mukaan.",
            description = "Listaa käyttöoikeusryhmät, jotka ovat mahdollisia pyynnössä annetulle organisaatiolle.")
    public List<KayttoOikeusRyhmaDto> listKayttoOikeusRyhmasByOrganisaatioOid(@PathVariable("organisaatioOid") String organisaatioOid) {
        return kayttoOikeusService.listPossibleRyhmasByOrganization(organisaatioOid);
    }

    @RequestMapping(value = "/{oid}/{organisaatioOid}", method = RequestMethod.GET)
    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPersonOrSelf(#oid, {'KAYTTOOIKEUS': {'READ', 'CRUD', 'PALVELUKAYTTAJA_CRUD'}}, null)")
    @Operation(summary = "Hakee henkilön voimassa olevat käyttöoikeusryhmät.",
            description = "Listaa kaikki annetun henkilön ja tämän annettuun organisaatioon "
                    + "liittyvät voimassaolevat sekä mahdollisesti myönnettävissä olevat "
                    + "käyttöoikeusryhmät DTO:n avulla.")
    public List<MyonnettyKayttoOikeusDto> listKayttoOikeusRyhmasIncludingHenkilos(
            @PathVariable("oid") String oid, @PathVariable("organisaatioOid") String organisaatioOid) {
        return kayttoOikeusService.listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos(oid, organisaatioOid, UserDetailsUtil.getCurrentUserOid());
    }

    @RequestMapping(value = "/henkilo/{oid}", method = RequestMethod.GET)
    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPersonOrSelf(#oid, {'KAYTTOOIKEUS': {'READ', 'CRUD', 'PALVELUKAYTTAJA_CRUD'}}, null)")
    @Operation(summary = "Hakee henkilön käyttöoikeusryhmät.",
            description = "Listaa henkilön kaikki käyttöoikeusryhmät sekä rajaa ne "
                    + "tiettyyn organisaatioon, jos kutsussa on annettu organisaatiorajoite.")
    public List<MyonnettyKayttoOikeusDto> listKayttoOikeusRyhmaByHenkilo(
            @PathVariable("oid") String oid,
            @RequestParam(value = "ooid", required = false) String organisaatioOid) {
        return kayttoOikeusService.listMyonnettyKayttoOikeusRyhmasByHenkiloAndOrganisaatio(oid, organisaatioOid);
    }

    @RequestMapping(value = "/henkilo/current", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Hakee kirjautuneen henkilön käyttöoikeusryhmät.",
            description = "Listaa nykyisen kirjautuneen henkilön kaikki käyttöoikeusryhmät "
                    + "sekä rajaa ne tiettyyn organisaatioon, jos kutsussa on "
                    + "annettu organisaatiorajoite.")
    public List<MyonnettyKayttoOikeusDto> listKayttoOikeusRyhmaForCurrentUser(
            @RequestParam(value = "ooid", required = false) String organisaatioOid) {
        return kayttoOikeusService.listMyonnettyKayttoOikeusRyhmasByHenkiloAndOrganisaatio(UserDetailsUtil.getCurrentUserOid(), organisaatioOid);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_KAYTTOOIKEUSRYHMIEN_LUKU',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Hakee käyttöoikeusryhmän tiedot.",
            description = "Hakee yhden käyttöoikeusryhmän kaikki tiedot "
                    + "annetun käyttöoikeusryhmän ID:n avulla.")
    public KayttoOikeusRyhmaDto getKayttoOikeusRyhma(@PathVariable("id") Long id,
                                                     @RequestParam(required = false) Boolean passiiviset) {
        return kayttoOikeusService.findKayttoOikeusRyhma(id, passiiviset);
    }


    @RequestMapping(value = "/{id}/sallitut", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Hakee käyttöoikeusryhmän rajoiteryhmät.",
            description = "Listaa kaikki käyttöoikeusryhmälle alistetut käyttöoikeusryhmät "
                    + "eli ne ryhmät jotka tämän ryhmän myöntäminen mahdollistaa.")
    public List<KayttoOikeusRyhmaDto> getSubRyhmasByKayttoOikeusRyhma(@PathVariable("id") Long id) {
        return kayttoOikeusService.findSubRyhmasByMasterRyhma(id);
    }

    @RequestMapping(value = "/{id}/kayttooikeus", method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_KAYTTOOIKEUSRYHMIEN_LUKU',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Hakee käyttöoikeusryhmään kuuluvat palvelut ja roolit.",
            description = "Listaa kaikki annettuun käyttöoikeusryhmään kuuluvat "
                    + "palvelut ja roolit yhdistelmäpareina DTO:n avulla.")
    public List<PalveluRooliDto> getKayttoOikeusByKayttoOikeusRyhma(@PathVariable("id") Long id) {
        return kayttoOikeusService.findPalveluRoolisByKayttoOikeusRyhma(id);
    }


    @RequestMapping(value = "/{id}/henkilot", method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Hakee käyttöoikeusryhmään kuuluvat henkilot",
            description = "Listaa kaikki annettuun käyttöoikeusryhmään kuuluvat henkilöt joilla voimassaoleva lupa")
    public RyhmanHenkilotDto getHenkilosByKayttoOikeusRyhma(@PathVariable("id") Long id) {
        return kayttoOikeusService.findHenkilotByKayttoOikeusRyhma(id);
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Luo uuden käyttöoikeusryhmän.",
            description = "Tekee uuden käyttöoikeusryhmän annetun DTO:n pohjalta.")
    @ResponseBody
    public Long createKayttoOikeusRyhma(@RequestBody @Validated KayttoOikeusRyhmaModifyDto uusiRyhma) {
        return kayttoOikeusService.createKayttoOikeusRyhma(uusiRyhma);
    }


    @PostMapping(value = "/kayttooikeus", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Luo uuden käyttöoikeuden.",
            description = "Luo uuden käyttöoikeuden annetun käyttöoikeus modelin pohjalta.")
    public KayttoOikeusDto createNewKayttoOikeus(@RequestBody @Validated KayttoOikeusCreateDto kayttoOikeus) {
        long id = kayttoOikeusService.createKayttoOikeus(kayttoOikeus);
        return kayttoOikeusService.findKayttoOikeusById(id);
    }


    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Päivittää käyttöoikeusryhmän.",
            description = "Päivittää käyttöoikeusryhmän tiedot annetun DTO:n avulla.")
    public KayttoOikeusRyhmaDto updateKayttoOikeusRyhma(@PathVariable("id") Long id, @RequestBody @Validated KayttoOikeusRyhmaModifyDto ryhmaData) {
        kayttoOikeusService.updateKayttoOikeusForKayttoOikeusRyhma(id, ryhmaData);
        return kayttoOikeusService.findKayttoOikeusRyhma(id, true);
    }

    @PutMapping(value = "/{id}/passivoi", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Passivoi käyttöoikeusryhmän.",
            description = "Passivoi käyttöoikeusryhmän. ")
    public void passivoiKayttoOikeusRyhma(@PathVariable("id") Long id) {
        kayttoOikeusService.passivoiKayttooikeusryhma(id);
    }

    @PutMapping(value = "/{id}/aktivoi", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Aktivoi käyttöoikeusryhmän.",
            description = "Aktivoi passivoidun käyttöoikeusryhmän.")
    public void aktivoiKayttoOikeusRyhma(@PathVariable("id") Long id) {
        this.kayttoOikeusService.aktivoiKayttooikeusryhma(id);
    }

    @PostMapping(value = "/ryhmasByKayttooikeus", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_KAYTTOOIKEUSRYHMIEN_LUKU',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Listaa käyttöoikeusryhmät käyttooikeuksien mukaan.",
            description = "Hakee käyttöoikeusryhmät joissa esiintyy jokin annetuista käyttöoikeuksista.")
    public List<KayttoOikeusRyhmaDto> getKayttoOikeusRyhmasByKayttoOikeus(
            @Parameter(description = "Format: {\"PALVELU\": \"ROOLI\", ...}") @RequestBody Map<String, String> kayttoOikeusList) {
        return kayttoOikeusService.findKayttoOikeusRyhmasByKayttoOikeusList(kayttoOikeusList);
    }

}
