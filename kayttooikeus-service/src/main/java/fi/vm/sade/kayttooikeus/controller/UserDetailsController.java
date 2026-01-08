package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.CasUserAttributes;
import fi.vm.sade.kayttooikeus.dto.LoginDto;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Hidden
@RestController
@RequestMapping(value = "/userDetails", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserDetailsController {
    private final KayttajatiedotService kayttajatiedotService;

    // Palomuurilla rajoitettu pääsy vain verkon sisältä
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public CasUserAttributes getByUsernameAndPassword(@Valid @RequestBody LoginDto dto) {
        Kayttajatiedot kayttajatiedot = kayttajatiedotService.getByUsernameAndPassword(dto.getUsername(), dto.getPassword());
        var roles = kayttajatiedotService.fetchKayttooikeudet(kayttajatiedot.getHenkilo().getOidHenkilo());
        return CasUserAttributes.fromKayttajatiedot(kayttajatiedot, roles);
    }
}