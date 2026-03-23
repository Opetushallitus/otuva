package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.KayttooikeusPerustiedotDto;
import fi.vm.sade.kayttooikeus.dto.PalveluKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.repositories.criteria.KayttooikeusCriteria;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/kayttooikeus", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Käyttöoikeuksien käsittelyyn liittyvät operaatiot.")
@RequiredArgsConstructor
public class KayttoOikeusController {
    private final KayttoOikeusService kayttoOikeusService;

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_READ',"
            + "'ROLE_APP_KAYTTOOIKEUS_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Hakee palveluun liittyvät käyttöoikeudet.",
            description = "Listaa kaikki palveluun liitetyt käyttöoikeudet "
                    + "palvelu-käyttöoikeus DTO:n avulla, johon on asetettu "
                    + "roolinimi ja sen lokalisoidut tekstit.")
    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public List<PalveluKayttoOikeusDto> listKayttoOikeusByPalvelu(
            @Parameter(description = "Palvelun tunnistenimi") @PathVariable("name") String name) {
        return kayttoOikeusService.listKayttoOikeusByPalvelu(name);
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_READ',"
            + "'ROLE_APP_KAYTTOOIKEUS_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Hakee käyttäjien käyttöoikeudet annetuilla hakukriteereillä. Haku rajoitettu 1000 kerralla.")
    @RequestMapping(value = "/kayttaja", method = RequestMethod.GET)
    public List<KayttooikeusPerustiedotDto> listKayttoOikeusByOid(KayttooikeusCriteria criteria,
                                                                  @RequestParam(required = false) Long offset) {
        long limit = 1000L;
        return this.kayttoOikeusService.listMyonnettyKayttoOikeusForUser(criteria, limit, offset);
    }
}
