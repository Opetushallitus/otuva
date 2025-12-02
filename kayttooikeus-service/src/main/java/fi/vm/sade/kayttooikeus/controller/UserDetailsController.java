package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.CasUserAttributes;
import fi.vm.sade.kayttooikeus.dto.LoginDto;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.exception.GoneException;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Hidden
@RestController
@RequestMapping(value = "/userDetails", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserDetailsController {

    private final UserDetailsService userDetailsService;
    private final KayttajatiedotService kayttajatiedotService;

    @Value("${kayttooikeus.userdetails.enabled}")
    public Boolean userdetailsEnabled;

    // Palomuurilla rajoitettu pääsy vain verkon sisältä
    @GetMapping("/{username}")
    public UserDetails getUserDetails(@PathVariable String username) {
        if (!userdetailsEnabled) {
            throw new GoneException("endpoint disabled (see: https://wiki.eduuni.fi/x/kc8hHw)");
        }
        try {
            return userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            throw new NotFoundException(e);
        }
    }

    // Palomuurilla rajoitettu pääsy vain verkon sisältä
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public CasUserAttributes getByUsernameAndPassword(@Valid @RequestBody LoginDto dto) {
        Kayttajatiedot kayttajatiedot = kayttajatiedotService.getByUsernameAndPassword(dto.getUsername(), dto.getPassword());
        var roles = kayttajatiedotService.fetchKayttooikeudet(kayttajatiedot.getHenkilo().getOidHenkilo());
        return CasUserAttributes.fromKayttajatiedot(kayttajatiedot, roles);
    }
}