package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeusHistoriaDto;
import fi.vm.sade.kayttooikeus.dto.KayttooikeusPerustiedotDto;
import fi.vm.sade.kayttooikeus.dto.PalveluKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.repositories.criteria.KayttooikeusCriteria;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.TaskExecutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@RestController
@RequestMapping(value = "/kayttooikeus", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Käyttöoikeuksien käsittelyyn liittyvät operaatiot.")
public class KayttoOikeusController {
    private KayttoOikeusService kayttoOikeusService;
    private TaskExecutorService taskExecutorService;

    public KayttoOikeusController(KayttoOikeusService kayttoOikeusService, TaskExecutorService taskExecutorService) {
        this.kayttoOikeusService = kayttoOikeusService;
        this.taskExecutorService = taskExecutorService;
    }

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
    @Operation(summary = "Hakee kirjautuneen käyttäjän käyttöoikeudet.",
            description = "Listaa kaikki nykyisen sisäänkirjautuneen käyttäjän käyttöoikeudet, "
                    + "jossa on mukana myös vanhentuneet käyttöoikeudet.")
    @RequestMapping(value = "/kayttaja/current", method = RequestMethod.GET)
    public List<KayttoOikeusHistoriaDto> listKayttoOikeusCurrentUser() {
        return this.kayttoOikeusService.listMyonnettyKayttoOikeusHistoriaForCurrentUser();
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

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Lähettää muistutusviestit henkilöille joilla on käyttöoikeus päättymässä.",
            description = "Tämä on alustavasti vain automaattisen sähköpostimuistutuksen testausta varten.")
    @PostMapping(value = "/expirationReminders", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String sendExpirationReminders(@Parameter(description =  "Vuosi", required = true) @RequestParam("year") int year,
                                       @Parameter(description =  "Kuukausi", required = true) @RequestParam("month") int month,
                                       @Parameter(description =  "Päivä", required = true) @RequestParam("day") int day) {
        Period expireThreshold = Period.between(LocalDate.now(), LocalDate.of(year, month, day));
        return String.format("%d", taskExecutorService.sendExpirationReminders(expireThreshold));
    }
}
