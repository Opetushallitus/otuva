package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.IdentifiedHenkiloTypeDto;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import fi.vm.sade.kayttooikeus.service.IdentificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/cas")
@Api(value = "/cas", description = "CAS:a varten olevat rajapinnat.")
@RequiredArgsConstructor
public class CasController {

    private final IdentificationService identificationService;
    private final HenkiloService henkiloService;

    @ApiOperation(value = "Generoi autentikointitokenin henkilölle.",
            notes = "Generoi tokenin CAS autentikointia varten henkilölle annettujen IdP tunnisteiden pohjalta.")
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_READ',"
            + "'ROLE_APP_HENKILONHALLINTA_READ_UPDATE',"
            + "'ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @RequestMapping(value = "/auth/oid/{oid}", method = RequestMethod.GET)
    public String generateAuthTokenForHenkilo(@PathVariable("oid") String oid,
                                              @RequestParam("idpkey") String idpKey,
                                              @RequestParam("idpid") String idpIdentifier) {
        return identificationService.generateAuthTokenForHenkilo(oid, idpKey, idpIdentifier);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_READ',"
            + "'ROLE_APP_HENKILONHALLINTA_READ_UPDATE',"
            + "'ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation(value = "Hakee henkilön OID:n autentikaation perusteella.",
            notes = "Hakee henkilön OID:n annettujen IdP tunnisteiden perusteella.")
    @RequestMapping(value = "/auth/idp/{idpkey}", method = RequestMethod.GET)
    public String getHenkiloOidByIdPAndIdentifier(@PathVariable("idpkey") String idpKey,
                                                  @RequestParam("idpid") String idpIdentifier) {
        return identificationService.getHenkiloOidByIdpAndIdentifier(idpKey, idpIdentifier);
    }

    @PreAuthorize("hasRole('ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation("Palauttaa tiedon henkilön aiemmasta vahvasta tunnistautumisesta")
    @RequestMapping(value = "/auth/henkilo/{oidHenkilo}/vahvastiTunnistettu", method = RequestMethod.GET)
    public boolean isVahvastiTunnistettu(@PathVariable String oidHenkilo) {
        return this.henkiloService.isVahvastiTunnistettu(oidHenkilo);
    }

    @PreAuthorize("hasRole('ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation("Palauttaa tiedon henkilön aiemmasta vahvasta tunnistautumisesta")
    @RequestMapping(value = "/auth/henkilo/{oidHenkilo}/loginToken", method = RequestMethod.GET)
    public String createLoginToken(@PathVariable String oidHenkilo) {
        return this.identificationService.createLoginToken(oidHenkilo);
    }

    // Palomuurilla rajoitettu pääsy vain verkon sisältä
    @ApiOperation(value = "Hakee henkilön identiteetitiedot.",
            notes = "Hakee henkilön identieettitiedot annetun autentikointitokenin avulla ja invalidoi autentikointitokenin.")
    @RequestMapping(value = "/auth/token/{token}", method = RequestMethod.GET)
    public IdentifiedHenkiloTypeDto getIdentityByAuthToken(@PathVariable("token") String authToken) throws IOException {
        return identificationService.findByTokenAndInvalidateToken(authToken);
    }

    // Palomuurilla rajoitettu pääsy vain verkon sisältä
    @ApiOperation(value = "Luo tai päivittää henkilön identiteetitiedot ja palauttaa kertakäyttöisen autentikointitokenin.",
            notes = "Luo tai päivittää henkilön identiteetitiedot hetun mukaan ja palauttaa kertakäyttöisen autentikointitokenin.")
    @RequestMapping(value = "/henkilo/{hetu}", method = RequestMethod.GET)
    public String updateIdentificationAndGenerateTokenForHenkiloByHetu(@PathVariable("hetu") String hetu) throws IOException {
        return identificationService.updateIdentificationAndGenerateTokenForHenkiloByHetu(hetu);
    }

    // Palomuurilla rajoitettu pääsy vain verkon sisältä
    @ApiOperation(value = "Virkailijan hetu-tunnistuksen jälkeinen käsittely. (rekisteröinti, hetu tunnistuksen pakotus, " +
            "mahdollinen kirjautuminen suomi.fi:n kautta.)")
    @RequestMapping(value = "/tunnistus", method = RequestMethod.GET)
    public void requestGet(HttpServletResponse response,
                             @RequestParam(value="loginToken", required = false) String loginToken,
                             @RequestParam(value="kutsuToken", required = false) String kutsuToken,
                             @RequestHeader(value = "nationalidentificationnumber", required = false) String hetu,
                             @RequestHeader(value = "firstname", required = false) String etunimet,
                             @RequestHeader(value = "sn", required = false) String sukunimi) throws IOException {

        // Tarkista että vaaditut tokenit ja tiedot löytyvät (riippuen casesta) -> Error sivu

        // Vaihdetaan kutsuToken väliaikaiseen ja tallennetaan tiedot vetumasta
        if (kutsuToken != null) {
            // Tallenna valitut headerit kutsu token kannan tauluun
            //displayname=[Anna Testi], cn=[Testi Anna Osuuspankki], givenname=[Anna], firstname=[Anna Osuuspankki], sn=[Testi], nationalidentificationnumber=[081181-9984], kotikuntakuntanumero=[019], kotikuntakuntas=[Helsinki], kotikuntakuntar=[], vakinainenkotimainenlahiosoites=[Osuuspankkitie 2], vakinainenkotimainenlahiosoiter=[], vakinainenkotimainenlahiosoitepostinumero=[00120], vakinainenkotimainenlahiosoitepostitoimipaikkas=[Helsinki], vakinainenkotimainenlahiosoitepostitoimipaikkar=[], vakinainenulkomainenlahiosoite=[], vakinainenulkomainenlahiosoitepaikkakuntajavaltios=[], vakinainenulkomainenlahiosoitepaikkakuntajavaltior=[], vakinainenulkomainenlahiosoitepaikkakuntajavaltioselvakielinen=[], vakinainenulkomainenlahiosoitevaltiokoodi3=[], tilapainenkotimainenlahiosoitelahiosoites=[], tilapainenkotimainenlahiosoitelahiosoiter=[], tilapainenkotimainenlahiosoitepostinumero=[], tilapainenkotimainenlahiosoitepostitoimipaikkas=[], tilapainenkotimainenlahiosoitepostitoimipaikkar=[]
            String temporaryKutsuToken = this.identificationService.updateKutsuAndGenerateTemporaryKutsuToken(
                    kutsuToken, hetu, etunimet, sukunimi);
            // Vaihda kutsutoken, lyhyeen tunnin kertakäyttöiseen "session" tokeniin
            // Tee redirect henkilo-ui:seen "session" tokeni query parametrinä
            response.sendRedirect("/henkilo-ui/rekisteroidy?temporaryKutsuToken=" + temporaryKutsuToken);
        }
        // Kirjataan henkilön vahva tunnistautuminen järjestelmään
        else if (loginToken != null) {
            // Hae henkilön tiedot jotka liittyvät logintokeniin
            // Päivitä henkilölle hetu jos ei ole ennestään olemassa ja merkitse se vahvistetuksi. Muuten tarkista, että hetu täsmää.
            // Luo auth token
            // Redirectaa CAS:iin auth tokenin kanssa.
            throw new UnsupportedOperationException();
//            String authToken = this.identificationService
//                    .updateIdentificationAndGenerateTokenForHenkiloByHetu(hetu);
//            response.sendRedirect("/cas/login?authToken=" + authToken);
        }
    }

    @ApiOperation(value = "Auttaa CAS session avaamisessa käyttöoikeuspalveluun.",
            notes = "Jos kutsuja haluaa tehdä useita rinnakkaisia kutsuja eikä CAS sessiota ole vielä avattu, " +
                    "täytyy tätä kutsua ensin.",
            authorizations = @Authorization("login"),
            response = ResponseEntity.class)
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/prequel", method = RequestMethod.GET)
    public ResponseEntity<String> requestGet() {
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @ApiOperation(value = "Auttaa CAS session avaamisessa käyttöoikeuspalveluun.",
            notes = "Jos kutsuja haluaa tehdä useita rinnakkaisia kutsuja eikä CAS sessiota ole vielä avattu, " +
                    "täytyy tätä kutsua ensin.",
            authorizations = @Authorization("login"),
            response = ResponseEntity.class)
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/prequel", method = RequestMethod.POST)
    public ResponseEntity<String> requestPost() {
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }
}
