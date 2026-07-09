package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.CasUserAttributes;
import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.dto.enumeration.LogInRedirectType;
import fi.vm.sade.kayttooikeus.dto.enumeration.LoginTokenValidationCode;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.service.EmailVerificationService;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import fi.vm.sade.kayttooikeus.service.IdentificationService;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.VirkailijaService;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import static fi.vm.sade.kayttooikeus.service.external.impl.HttpClientUtil.noContentOrNotFoundException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/cas", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "CAS:a varten olevat rajapinnat.")
@RequiredArgsConstructor
public class CasController {
    private final IdentificationService identificationService;
    private final HenkiloService henkiloService;
    private final EmailVerificationService emailVerificationService;
    private final KayttajatiedotService kayttajatiedotService;
    private final VirkailijaService virkailijaService;
    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @Operation(summary = "Generoi autentikointitokenin henkilölle.",
            description = "Generoi tokenin CAS autentikointia varten henkilölle annettujen IdP tunnisteiden pohjalta.")
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @RequestMapping(value = "/auth/oid/{oid}", method = RequestMethod.GET)
    public String generateAuthTokenForHenkilo(@PathVariable("oid") String oid,
                                              @RequestParam("idpkey") String idpKey,
                                              @RequestParam("idpid") String idpIdentifier) {
        return identificationService.generateAuthTokenForHenkilo(oid, idpKey, idpIdentifier);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Hakee henkilön OID:n autentikaation perusteella.",
            description = "Hakee henkilön OID:n annettujen IdP tunnisteiden perusteella.")
    @RequestMapping(value = "/auth/idp/{idpkey}", method = RequestMethod.GET)
    public String getHenkiloOidByIdPAndIdentifier(@PathVariable("idpkey") String idpKey,
                                                  @RequestParam("idpid") String idpIdentifier) {
        return identificationService.getHenkiloOidByIdpAndIdentifier(idpKey, idpIdentifier);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Palauttaa tiedon henkilön aiemmasta vahvasta tunnistautumisesta")
    @RequestMapping(value = "/auth/henkilo/{oidHenkilo}/vahvastiTunnistettu", method = RequestMethod.GET)
    public boolean isVahvastiTunnistettu(@PathVariable String oidHenkilo) {
        return this.henkiloService.isVahvastiTunnistettu(oidHenkilo);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Palauttaa tiedon henkilön aiemmasta vahvasta tunnistautumisesta")
    @RequestMapping(value = "/auth/henkilo/username/{username}/vahvastiTunnistettu", method = RequestMethod.GET)
    public boolean isVahvastiTunnistettuByUsername(@PathVariable String username) {
        return this.henkiloService.isVahvastiTunnistettuByUsername(username);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Palauttaa uri:n johon käyttäjä tulee ohjata kirjautumisen yhteydessä, tai null jos uudelleenohjausta ei tarvita")
    @RequestMapping(value = "/auth/henkilo/{oidHenkilo}/logInRedirect", method = RequestMethod.GET)
    public LogInRedirectType logInRedirectByOidHenkilo(@PathVariable("oidHenkilo") String oidHenkilo) {
        return this.henkiloService.logInRedirectByOidhenkilo(oidHenkilo);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Palauttaa uri:n johon käyttäjä tulee ohjata kirjautumisen yhteydessä, tai null jos uudelleenohjausta ei tarvita")
    @RequestMapping(value = "/auth/henkilo/username/{username}/logInRedirect", method = RequestMethod.GET)
    public LogInRedirectType logInRedirectByUsername(@PathVariable("username") String username) {
        return this.henkiloService.logInRedirectByUsername(username);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Luo tilapäisen tokenin henkilön vahvan tunnistaumisen ajaksi")
    @RequestMapping(value = "/auth/henkilo/{oidHenkilo}/loginToken", method = RequestMethod.GET)
    public String createLoginToken(@PathVariable String oidHenkilo, @RequestParam(required = false) Boolean salasananVaihto) {
        return this.identificationService.createLoginToken(oidHenkilo, salasananVaihto, null);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @PostMapping(value = "/auth", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CasUserAttributes getByUsernameAndPassword(@Valid @RequestBody LoginDto dto) {
        Kayttajatiedot kayttajatiedot = kayttajatiedotService.getByUsernameAndPassword(dto.getUsername(), dto.getPassword());
        var roles = kayttajatiedotService.fetchKayttooikeudet(kayttajatiedot.getHenkilo().getOidHenkilo());
        return CasUserAttributes.fromKayttajatiedot(kayttajatiedot, roles);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Hakee henkilön identiteetitiedot.",
            description = "Hakee henkilön identieettitiedot annetun autentikointitokenin avulla ja invalidoi autentikointitokenin.")
    @RequestMapping(value = "/auth/token/{token}", method = RequestMethod.GET)
    public CasUserAttributes getIdentityByAuthToken(@PathVariable("token") String authToken) {
        var identification = identificationService.findByTokenAndInvalidateToken(authToken);
        var roles = kayttajatiedotService.fetchKayttooikeudet(identification.getHenkilo().getOidHenkilo());
        return CasUserAttributes.fromIdentification(identification, roles);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Hakee henkilön CAS-attribuutit")
    @GetMapping(value = "/auth/henkilo/{oid}")
    public CasUserAttributes getIdentityByOid(@PathVariable String oid) {
        var kayttaja = kayttajatiedotService.getByHenkiloOid(oid);
        var roles = kayttajatiedotService.fetchKayttooikeudet(oid);
        return CasUserAttributes.fromKayttajatiedotReadDto(oid, kayttaja, roles);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Hakee henkilön CAS-attribuutit")
    @GetMapping(value = "/auth/identification/{idpEntityId}/{identifier}")
    public CasUserAttributes getIdentityByIdpIdentifier(@PathVariable String idpEntityId, @PathVariable String identifier) {
        var oid = identificationService.getHenkiloOidByIdpAndIdentifier(idpEntityId, identifier);
        var kayttaja = kayttajatiedotService.getByHenkiloOid(oid);
        var roles = kayttajatiedotService.fetchKayttooikeudet(oid);
        return CasUserAttributes.fromKayttajatiedotReadDto(oid, kayttaja, roles);
    }

    record HetuDto(String hetu) {}

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Hakee henkilön CAS-attribuutit")
    @PostMapping(value = "/auth/hetu")
    public CasUserAttributes getIdentityByHetu(@RequestBody @Valid HetuDto dto) {
        var henkilo = oppijanumerorekisteriClient.getHenkiloByHetu(dto.hetu)
                .orElseThrow(() -> noContentOrNotFoundException(""));
        var kayttaja = kayttajatiedotService.getByHenkiloOid(henkilo.getOidHenkilo());
        var roles = kayttajatiedotService.fetchKayttooikeudet(henkilo.getOidHenkilo());
        return CasUserAttributes.fromKayttajatiedotReadDto(henkilo.getOidHenkilo(), kayttaja, roles);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Rekisteröi virkailijan ja palauttaa CAS-attribuutit")
    @PostMapping(value = "/register")
    public CasUserAttributes registerVirkailija(@RequestBody @Valid VirkailijaRegistration dto) {
        String oid = virkailijaService.register(dto);
        var kayttaja = kayttajatiedotService.getByHenkiloOid(oid);
        var roles = kayttajatiedotService.fetchKayttooikeudet(oid);
        return CasUserAttributes.fromKayttajatiedotReadDto(oid, kayttaja, roles);
    }

    @Operation(summary = "Auttaa CAS session avaamisessa käyttöoikeuspalveluun.",
            description = "Jos kutsuja haluaa tehdä useita rinnakkaisia kutsuja eikä CAS sessiota ole vielä avattu, täytyy tätä kutsua ensin.")
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/prequel", method = RequestMethod.GET)
    public ResponseEntity<String> requestGet() {
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @Operation(summary = "Auttaa CAS session avaamisessa käyttöoikeuspalveluun.",
            description = "Jos kutsuja haluaa tehdä useita rinnakkaisia kutsuja eikä CAS sessiota ole vielä avattu, täytyy tätä kutsua ensin.")
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/prequel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> requestPost() {
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @PostMapping(value = "/salasananvaihto", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Vaihtaa käyttäjän salasanan tilapäisen loginTokenin perusteella")
    public CasRedirectParametersResponse changePassword(@RequestBody @Validated ChangePasswordRequest changePassword) {
        return kayttajatiedotService.changePassword(changePassword);
    }

    @PostMapping(value = "/emailverification/{loginToken}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Asettaa käyttäjän sähköpostiosoitteet vahvistetuksi")
    public CasRedirectParametersResponse emailVerification(@RequestBody @Validated HenkiloUpdateDto henkiloUpdate,
                                                          @PathVariable String loginToken) {
        return this.emailVerificationService.emailVerification(henkiloUpdate, loginToken);
    }

    @GetMapping(value = "/emailverification/loginTokenValidation/{loginToken}")
    @Operation(summary = "Palauttaa validatointikoodin loginTokenille",
            description = "Validointikoodista käyttöliittymässä tiedetään täytyykö käyttäjälle näyttää virhesivu")
    public LoginTokenValidationCode getLoginTokenValidationCode(@PathVariable String loginToken) {
        return this.emailVerificationService.getLoginTokenValidationCode(loginToken);
    }

    @GetMapping(value = "/henkilo/loginToken/{loginToken}")
    @Operation(summary = "Hakee käyttäjän tiedot loginTokenin perusteella")
    public HenkiloDto getUserByLoginToken(@PathVariable("loginToken") String loginToken) {
        return this.emailVerificationService.getHenkiloByLoginToken(loginToken);
    }

    @Operation(summary = "Deprekoitu CAS palvelusta siirretty rajapinta",
            description = "Deprekoitu. Käytä /henkilo/current/omattiedot ja oppijanumerorekisterin /henkilo/current/omattiedot rajapintoja.")
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public MeDto getMe() {
        return this.henkiloService.getMe();
    }

    @Operation(summary = "Deprekoitu CAS palvelusta siirretty rajapinta",
            description = "Deprekoitu. Käytä /henkilo/current/omattiedot rajapintaa.")
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/myroles", method = RequestMethod.GET)
    public List<String> getMyroles() {
        return this.henkiloService.getMyRoles();
    }
}
