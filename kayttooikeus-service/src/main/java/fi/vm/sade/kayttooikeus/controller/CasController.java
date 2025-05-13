package fi.vm.sade.kayttooikeus.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import fi.vm.sade.kayttooikeus.CasUserAttributes;
import fi.vm.sade.kayttooikeus.config.security.casoppija.SuomiFiAuthenticationDetails;
import fi.vm.sade.kayttooikeus.config.security.casoppija.SuomiFiUserDetails;
import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.dto.enumeration.LogInRedirectType;
import fi.vm.sade.kayttooikeus.dto.enumeration.LoginTokenValidationCode;
import fi.vm.sade.kayttooikeus.service.EmailVerificationService;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import fi.vm.sade.kayttooikeus.service.IdentificationService;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import fi.vm.sade.kayttooikeus.service.VahvaTunnistusService;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/cas", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "CAS:a varten olevat rajapinnat.")
@RequiredArgsConstructor
public class CasController {

    private final IdentificationService identificationService;
    private final HenkiloService henkiloService;
    private final VahvaTunnistusService vahvaTunnistusService;
    private final EmailVerificationService emailVerificationService;
    private final KayttajatiedotService kayttajatiedotService;
    private final KutsuService kutsuService;
    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @Value("${kayttooikeus.registration.allow-test-suomifi:false}")
    private String allowTestSuomifi;
    @Value("${url-virkailija}")
    private String urlVirkailija;
    @Value("${cas.oppija.logout}")
    private String casOppijaLogout;
    @Value("${virkailijan-tyopoyta}")
    private String virkailijanTyopoytaUrl;

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

    @PutMapping(value = "/hakaregistration/{temporaryToken}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Kutsun päivittäminen väliaikaisella tokenilla. Sallii osittaisen päivittämisen.")
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    public CasUserAttributes hakaRegistration(@PathVariable String temporaryToken, @RequestBody KutsuUpdateDto kutsuUpdateDto) {
        HenkiloUpdateDto henkiloUpdateDto = this.kutsuService.createHenkiloWithHakaIdentifier(temporaryToken, kutsuUpdateDto.getHakaIdentifier());
        oppijanumerorekisteriClient.updateHenkilo(henkiloUpdateDto);
        identificationService.updateIdentificationAndGenerateTokenForHenkiloByOid(henkiloUpdateDto.getOidHenkilo());
        var kayttaja = kayttajatiedotService.getByHenkiloOid(henkiloUpdateDto.getOidHenkilo());
        var roles = kayttajatiedotService.fetchKayttooikeudet(henkiloUpdateDto.getOidHenkilo());
        return CasUserAttributes.fromKayttajatiedotReadDto(henkiloUpdateDto.getOidHenkilo(), kayttaja, roles);
    }

    @Operation(summary = "Virkailijan hetu-tunnistuksen jälkeinen käsittely. (rekisteröinti, hetu tunnistuksen pakotus, " +
            "mahdollinen kirjautuminen suomi.fi:n kautta.)")
    @RequestMapping(value = "/tunnistus", method = RequestMethod.GET)
    public void requestGet(HttpServletRequest request, HttpServletResponse response,
                           Principal principal,
                           @RequestParam(value="loginToken", required = false) String loginToken,
                           @RequestParam(value="kutsuToken", required = false) String kutsuToken,
                           @RequestParam(value = "locale", required = false) String kielisyys)
            throws IOException {
        SuomiFiUserDetails details = getSuomiFiAuthenticationDetails(principal);
        // kirjataan ulos, jotta virkailija-CAS ei hämmenny
        handleOppijaLogout(request, response);
        if (StringUtils.hasLength(kutsuToken)) {
            // Vaihdetaan kutsuToken väliaikaiseen ja tallennetaan tiedot vetumasta
            response.sendRedirect(getRedirectViaLoginUrl(
                    vahvaTunnistusService.kasitteleKutsunTunnistus(
                    kutsuToken, kielisyys, details.hetu,
                    details.etunimet, details.sukunimi)));
        } else if (StringUtils.hasLength(loginToken)) {
            // Kirjataan henkilön vahva tunnistautuminen järjestelmään, vaihe 1
            // Joko päästetään suoraan sisään tai käytetään lisätietojen keräyssivun kautta
            String redirectUrl = getRedirectViaLoginUrl(
                    getVahvaTunnistusRedirectUrl(loginToken, kielisyys, details.hetu));
            response.sendRedirect(redirectUrl);
        } else {
            response.sendRedirect(getRedirectViaLoginUrl(
                    vahvaTunnistusService.kirjaaKayttajaVahvallaTunnistuksella(details.hetu, kielisyys)));
        }
    }

    private SuomiFiUserDetails getSuomiFiAuthenticationDetails(Principal principal) {
        assert(principal != null);
        assert(principal instanceof PreAuthenticatedAuthenticationToken);
        PreAuthenticatedAuthenticationToken token = (PreAuthenticatedAuthenticationToken) principal;
        SuomiFiAuthenticationDetails details = (SuomiFiAuthenticationDetails) token.getDetails();
        if ("true".equals(allowTestSuomifi)) {
            return new SuomiFiUserDetails(
                details.hetu,
                details.sukunimi == null ? "Testinen" : details.sukunimi,
                details.etunimet == null ? "Testi" : details.etunimet
            );
        }
        return details.getSuomiFiUserDetails();
    }

    private String getVahvaTunnistusRedirectUrl(String loginToken, String kielisyys, String hetu) {
        try {
            return vahvaTunnistusService.kirjaaVahvaTunnistus(loginToken, kielisyys, hetu);
        } catch (Exception e) {
            log.error("User failed strong identification", e);
            return urlVirkailija + "/henkilo-ui/kayttaja/vahvatunnistusinfo/virhe/" + kielisyys + "/" + loginToken;
        }
    }

    private String getRedirectViaLoginUrl(String originalUrl) {
        // kierrätetään CAS-oppijan logoutista, jotta CAS-virkailijaa ei hämmennetä
        // sen sessiolla, tiketeillä tms.
        return UriComponentsBuilder.fromUriString(casOppijaLogout)
                .queryParam("service", URLEncoder.encode(originalUrl, StandardCharsets.UTF_8))
                .build()
                .toUriString();
    }

    @PostMapping(value = "/uudelleenrekisterointi", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Virkailijan uudelleenrekisteröinti")
    public VahvaTunnistusResponseDto tunnistauduVahvasti(
            @RequestParam(value = "kielisyys") String kielisyys,
            @RequestParam(value = "loginToken") String loginToken,
            @RequestBody @Valid VahvaTunnistusRequestDto dto) {
        // Kirjataan henkilön vahva tunnistautuminen järjestelmään, vaihe 2
        return vahvaTunnistusService.tunnistaudu(loginToken, dto);
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

    @GetMapping(value = "/loginparams", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Palauttaa CAS-kirjautumiseen vaaditut parametrit")
    public CasLoginParametersResponse getChangePasswordLoginParams() {
        return new CasLoginParametersResponse(virkailijanTyopoytaUrl);
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

    @GetMapping(value = "/emailverification/redirectByLoginToken/{loginToken}")
    @Operation(summary = "Palauttaa uudelleenohjausurlin loginTokenin perusteella.")
    public CasRedirectParametersResponse getFrontPageRedirectByLoginToken(@PathVariable String loginToken) {
        return this.emailVerificationService.redirectUrlByLoginToken(loginToken);
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
    public MeDto getMe() throws JsonProcessingException {
        return this.henkiloService.getMe();
    }

    @Operation(summary = "Deprekoitu CAS palvelusta siirretty rajapinta",
            description = "Deprekoitu. Käytä /henkilo/current/omattiedot rajapintaa.")
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/myroles", method = RequestMethod.GET)
    public List<String> getMyroles() {
        return this.henkiloService.getMyRoles();
    }

    private void handleOppijaLogout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
