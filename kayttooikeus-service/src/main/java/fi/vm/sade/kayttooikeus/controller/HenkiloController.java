package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.kayttooikeus.enumeration.OrderByHenkilohaku;
import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import fi.vm.sade.kayttooikeus.service.IdentificationService;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static fi.vm.sade.kayttooikeus.model.Identification.HAKA_AUTHENTICATION_IDP;

@RestController
@RequestMapping(value = "/henkilo", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Henkilöön liittyvät operaatiot")
@RequiredArgsConstructor
public class HenkiloController {

    private final OrganisaatioHenkiloService organisaatioHenkiloService;
    private final HenkiloService henkiloService;
    private final KayttajatiedotService kayttajatiedotService;
    private final IdentificationService identificationService;

    @GetMapping("/{oid}")
    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPersonOrSelf(#oid, {'KAYTTOOIKEUS': {'PALVELUKAYTTAJA_CRUD'}}, null)")
    @Operation(summary = "Hakee henkilön OID:n perusteella")
    public HenkiloReadDto getByOid(@PathVariable String oid) {
        return henkiloService.getByOid(oid);
    }

    @GetMapping("/kayttajatunnus={kayttajatunnus}")
    @PostAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPersonOrSelf(returnObject.oid, {'KAYTTOOIKEUS': {'READ', 'CRUD', 'PALVELUKAYTTAJA_CRUD'}}, null)")
    @Operation(summary = "Hakee henkilön käyttäjätunnuksen perusteella")
    public HenkiloReadDto getByKayttajatunnus(@PathVariable String kayttajatunnus) {
        return henkiloService.getByKayttajatunnus(kayttajatunnus);
    }

    @GetMapping("/{oid}/linkitykset")
    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPersonOrSelf(#oid, {'KAYTTOOIKEUS': {'PALVELUKAYTTAJA_CRUD'}}, null)")
    @Operation(summary = "Henkilön linkitystiedot")
    public HenkiloLinkitysDto getLinkitykset(@PathVariable String oid, @RequestParam(defaultValue = "false") boolean showPassive) {
        return this.henkiloService.getLinkitykset(oid, showPassive);
    }

    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPersonOrSelf(#oid, {'KAYTTOOIKEUS': {'READ', 'CRUD', 'PALVELUKAYTTAJA_CRUD'}}, #permissionService)")
    @Operation(summary = "Listaa henkilön aktiiviset organisaatiot (organisaatiohenkilöt) organisaatioiden tai " +
            "ryhmien tiedoilla rekursiivisesti.",
            description = "Hakee annetun henkilön aktiiviset ja suunnitellut organisaatiohenkilöt organisaation tai ryhmän tiedoilla siten, " +
                    "että organisaatio sisältää myös lapsiorganisaationsa rekursiivisesti. Oletuksena haetaan myös ne organisaatiot, joihin ei ole voimassa olevia käyttöoikeuksia. ")
    @RequestMapping(value = "/{oid}/organisaatio", method = RequestMethod.GET)
    public List<OrganisaatioHenkiloWithOrganisaatioDto> listOrganisatioHenkilos(
            @PathVariable @Parameter(description = "Henkilö-OID", required = true) String oid,
            @RequestParam(required = false, defaultValue = "fi") @Parameter(description = "Organisaatioiden järjestyksen kielikoodi (oletus fi)") String comparisonLangCode,
            @RequestParam(required = false) @Parameter(description = "Ylimääräinen suodatus, jolla mahdollisten organisaatiohenkilöiden tuloslistaa rajataan.") PalveluRooliGroup requiredRoles,
            @RequestHeader(value = "External-Permission-Service", required = false)
                    ExternalPermissionService permissionService) {
        return organisaatioHenkiloService.listOrganisaatioHenkilos(oid, comparisonLangCode, requiredRoles);
    }

    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPersonOrSelf(#henkiloOid, {'KAYTTOOIKEUS': {'READ', 'CRUD', 'PALVELUKAYTTAJA_CRUD'}}, #permissionService)")
    @Operation(summary = "Listaa henkilön organisaatiot.",
            description = "Hakee annetun henkilön kaikki organisaatiohenkilöt.")
    @RequestMapping(value = "/{oid}/organisaatiohenkilo", method = RequestMethod.GET)
    public List<OrganisaatioHenkiloDto> listOrganisaatioHenkilos(@PathVariable("oid") String henkiloOid,
                                                                 @RequestHeader(value = "External-Permission-Service", required = false)
                                                                         ExternalPermissionService permissionService) {
        return organisaatioHenkiloService.findOrganisaatioByHenkilo(henkiloOid);
    }

    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPerson(#henkiloOid, {'KAYTTOOIKEUS': {'READ', 'CRUD', 'PALVELUKAYTTAJA_CRUD'}}, null)")
    @Operation(summary = "Hakee henkilön yhden organisaation tiedot.",
            description = "Hakee henkilön yhden organisaatiohenkilön tiedot.")
    @RequestMapping(value = "/{oid}/organisaatiohenkilo/{organisaatioOid}", method = RequestMethod.GET)
    public OrganisaatioHenkiloDto findByOrganisaatioOid(@PathVariable("oid") String henkiloOid,
                                                        @PathVariable("organisaatioOid") String organisaatioOid) {
        return organisaatioHenkiloService.findOrganisaatioHenkiloByHenkiloAndOrganisaatio(henkiloOid, organisaatioOid);
    }

    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPersonOrSelf(#henkiloOid, {'KAYTTOOIKEUS': {'READ', 'CRUD', 'PALVELUKAYTTAJA_CRUD'}}, null)")
    @Operation(summary = "Hakee henkilön käyttäjätiedot.",
            description = "Hakee henkilön käyttäjätiedot.")
    @RequestMapping(value = "/{oid}/kayttajatiedot", method = RequestMethod.GET)
    public KayttajatiedotReadDto getKayttajatiedot(@PathVariable("oid") String henkiloOid) {
        return kayttajatiedotService.getByHenkiloOid(henkiloOid);
    }

    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPerson(#henkiloOid, {'KAYTTOOIKEUS': {'CRUD', 'PALVELUKAYTTAJA_CRUD'}}, null)")
    @Operation(summary = "Päivittää henkilön käyttäjätiedot.")
    @PutMapping(value = "/{oid}/kayttajatiedot", consumes = MediaType.APPLICATION_JSON_VALUE)
    public KayttajatiedotReadDto updateKayttajatiedot(@PathVariable("oid") String henkiloOid,
                                                        @RequestBody @Validated KayttajatiedotUpdateDto kayttajatiedot) {
        return kayttajatiedotService.updateKayttajatiedot(henkiloOid, kayttajatiedot);
    }

    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPersonOrSelf(#henkiloOid, {'KAYTTOOIKEUS': {'CRUD', 'PALVELUKAYTTAJA_CRUD'}}, null)")
    @PostMapping(value = "/{henkiloOid}/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Asettaa henkilön salasanan.",
            description = "Asettaa henkilölle uuden salasanan virkailijan "
                    + "toimesta, ei tee tarkistusta vanhalle salasanalle "
                    + "vaan yliajaa suoraan uudella.")
    public void setPassword( @Parameter(description = "Henkilön OID", required = true) @PathVariable("henkiloOid") String henkiloOid,
                                 @Parameter(description = "Format: \"password\"", required = true) @RequestBody String password) {
            this.kayttajatiedotService.changePasswordAsAdmin(henkiloOid, password);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @RequestMapping(value = "/{henkiloOid}/passivoi", method = RequestMethod.DELETE)
    @Operation(summary = "Passivoi henkilön kaikki organisaatiot ja käyttöoikeudet.",
            description = "Passivoi henkilön kaikki organisaatiot ja käyttöoikeudet. Kutsutaan oppijanumerorekisterin henkilön" +
                    "passivoinnin yhteydessä automaattisesti.")
    public void passivoi(@Parameter(description = "Henkilön OID", required = true) @PathVariable(value = "henkiloOid") String henkiloOid,
                         @Parameter(description = "Jos ei annettu käytetään kirjautunutta")
                         @RequestParam(value = "kasittelijaOid", required = false) String kasittelijaOid) {
        this.henkiloService.passivoi(henkiloOid, kasittelijaOid);
    }

    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPerson(#oid, {'KAYTTOOIKEUS': {'CRUD', 'PALVELUKAYTTAJA_CRUD'}}, #permissionService)")
    @RequestMapping(value = "/{oid}/hakatunnus", method = RequestMethod.GET)
    @Operation(summary = "Hakee henkilön Haka-tunnisteet.",
            description = "Hakee annetun henkilön Haka-tunnisteet.")
    public Set<String> getHenkilosHakaTunnisteet(@PathVariable("oid") @Parameter(description = "Henkilön OID") String oid,
                                                 @RequestHeader(value = "External-Permission-Service", required = false)
                                                                  ExternalPermissionService permissionService) {
        return identificationService.getTunnisteetByHenkiloAndIdp(HAKA_AUTHENTICATION_IDP, oid);
    }

    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPerson(#oid, {'KAYTTOOIKEUS': {'CRUD', 'PALVELUKAYTTAJA_CRUD'}}, #permissionService)")
    @PutMapping(value = "/{oid}/hakatunnus", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Päivittää henkilön Haka-tunnisteet. ",
            description = "Päivittää annetun henkilön Haka-tunnisteet.")
    public Set<String> updateHenkilosHakaTunnisteet(@PathVariable("oid") @Parameter(description = "Henkilön OID") String oid,
                                                 @RequestBody Set<String> hakatunnisteet,
                                                 @RequestHeader(value = "External-Permission-Service", required = false)
                                                                  ExternalPermissionService permissionService) {
        return identificationService.updateTunnisteetByHenkiloAndIdp(HAKA_AUTHENTICATION_IDP, oid, hakatunnisteet);

    }

    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPerson(#oid, {'KAYTTOOIKEUS': {'CRUD', 'PALVELUKAYTTAJA_CRUD'}}, #permissionService)")
    @GetMapping("/{oid}/idp/{identityProvider}")
    @Operation(summary = "Hakee henkilön linkitetyt tunnisteet.",
            description = "Hakee annetun henkilön linkitetyt tunnisteet.")
    public Set<String> getHenkilosLinkitetytTunnisteet(
            @PathVariable("oid") @Parameter(description = "Henkilön OID") String oid,
            @PathVariable("identityProvider") @Parameter(description = "Identity Provider")
            String identityProvider,
            @RequestHeader(value = "External-Permission-Service", required = false)
            ExternalPermissionService permissionService
    ) {
        return identificationService.getTunnisteetByHenkiloAndIdp(identityProvider, oid);
    }

    @PreAuthorize("@permissionCheckerServiceImpl.isAllowedToAccessPerson(#oid, {'KAYTTOOIKEUS': {'CRUD', 'PALVELUKAYTTAJA_CRUD'}}, #permissionService)")
    @PutMapping(value = "/{oid}/idp/{identityProvider}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Päivittää linkitetyt tunnisteet",
            description = "Päivittää annetun henkilön linkitetyt tunnisteet."
    )
    public Set<String> updateHenkilosLinkiettytTunnisteet(
            @PathVariable("oid") @Parameter(description = "Henkilön OID") String oid,
            @PathVariable("identityProvider") @Parameter(description = "Identity Provider") String identityProvider,
            @RequestBody Set<String> tunnisteet,
            @RequestHeader(value = "External-Permission-Service", required = false)
            ExternalPermissionService permissionService
    ) {
        return identificationService.updateTunnisteetByHenkiloAndIdp(identityProvider, oid, tunnisteet);
    }

    @GetMapping("/{oid}/kayttooikeudet")
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Palauttaa henkilöiden oid:t joiden tietoihin annetulla henkilöllä on oikeutus")
    public KayttooikeudetDto getKayttooikeudet(@PathVariable String oid, OrganisaatioHenkiloCriteria criteria) {
        return henkiloService.getKayttooikeudet(oid, criteria);
    }

    @PostMapping(value = "/{oid}/kayttooikeudet", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Palauttaa henkilöiden oid:t joiden tietoihin annetulla henkilöllä on oikeutus")
    public KayttooikeudetDto postKayttooikeudet(@PathVariable String oid, @RequestBody OrganisaatioHenkiloCriteria criteria) {
        return henkiloService.getKayttooikeudet(oid, criteria);
    }

    @PostMapping(value = "/henkilohaku", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "UI:ta varten tehty mahdollisesti HIDAS hakurajapinta. EI tarkoitettu palveluiden käyttöön. Muutosaltis.",
            description = "Palauttaa suppean setin henkilöiden tietoja annetuilla hakukriteereillä. Toimii eri tavalla eri käyttäjäryhmille! " +
                    "(rekisterinpitäjä, OPH:n virkaiilja, muu virkailija) Hakua rajoitetaan näille ryhmille joten ei tarvitse " +
                    "erillisiä käyttöoikeuksia. Hakutuloksen maksimikoko saattaa olla 100 tai 101 käyttäjätunnuksella " +
                    "haun takia.")
    public Collection<HenkilohakuResultDto> henkilohaku(@Validated @RequestBody HenkilohakuCriteriaDto henkilohakuCriteriaDto,
                                                        @RequestParam(defaultValue = "0") Long offset,
                                                        @RequestParam(required = false) OrderByHenkilohaku orderBy) {
        return this.henkiloService.henkilohaku(henkilohakuCriteriaDto, offset, orderBy);
    }

    @PostMapping(value = "/henkilohakucount", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "UI:ta varten tehty mahdollisesti HIDAS hakurajapinta palauttaa henkilohaun tulosten lukumäärän ilman sivutusrajoitusta",
            description = "Palauttaa annetuilla rajoitteilla löytyvän henkilöjoukon koon")
    public Long henkilohakuCount(@Validated @RequestBody HenkilohakuCriteriaDto henkilohakuCriteriaDto ) {
        return this.henkiloService.henkilohakuCount(henkilohakuCriteriaDto);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/current/omattiedot", method = RequestMethod.GET)
    @Operation(summary = "Palauttaa henkilön tiedot käyttöoikeuspalvelun näkökulmasta")
    public OmatTiedotDto omatTiedot() {
        return this.henkiloService.getOmatTiedot();
    }

    @PutMapping(value = "/{oid}/anomusilmoitus", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Anomusilmoitus asetuksen muuttaminen")
    @PreAuthorize("@permissionCheckerServiceImpl.isCurrentUserAdmin()")
    public void updateAnomusilmoitus(@PathVariable String oid,
                                     @Parameter(description = "Lista käyttöoikeusryhmä ID:itä. Formaatti: [id1, id2,...]") @RequestBody Set<Long> anomusilmoitusKayttooikeusRyhmat) {
        this.henkiloService.updateAnomusilmoitus(oid, anomusilmoitusKayttooikeusRyhmat);
    }

}