package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.KayttajaReadDto;
import fi.vm.sade.kayttooikeus.dto.VirkailijaCreateDto;
import fi.vm.sade.kayttooikeus.dto.VirkailijaCriteriaDto;
import fi.vm.sade.kayttooikeus.service.VirkailijaService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/virkailija", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class VirkailijaController {

    private final VirkailijaService virkailijaService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_APP_KAYTTOOIKEUS_VIRKAILIJANLUONTI')")
    @Operation(summary = "Virkailijan luonti",
            description = "Tarkoitettu vain testikäyttöön, tuotannossa virkailijat luodaan kutsun kautta.")
    public String create(@Valid @RequestBody VirkailijaCreateDto dto) {
        return virkailijaService.create(dto);
    }

    @PostMapping(value = "/haku", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_READ'," +
            "'ROLE_APP_KAYTTOOIKEUS_CRUD'," +
            "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @Operation(summary = "Virkailijoiden haku")
    public Iterable<KayttajaReadDto> list(@RequestBody VirkailijaCriteriaDto criteria) {
        return virkailijaService.list(criteria);
    }

}
