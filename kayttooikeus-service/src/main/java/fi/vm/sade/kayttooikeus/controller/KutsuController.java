package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto;
import fi.vm.sade.kayttooikeus.dto.KutsuReadDto;
import fi.vm.sade.kayttooikeus.enumeration.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.repositories.criteria.KutsuCriteria;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

@RestController
@RequestMapping(value = "/kutsu", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Virkailijan kutsumiseen liittyvät toiminnot")
public class KutsuController {
    private final KutsuService kutsuService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Hakee kutsut annettujen hakuehtojen perusteella",
            description = "Haun tulos riippuu käyttäjän oikeuksista (rekisterinpitäjä, Oph-virkailija, normaali käyttäjä)")
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_READ',"
            + "'ROLE_APP_KAYTTOOIKEUS_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_KUTSU_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    public List<KutsuReadDto> listKutsus(
            KutsuCriteria kutsuCriteria,
            @Parameter(description = "Järjestysperuste") @RequestParam(required = false, defaultValue = "AIKALEIMA") KutsuOrganisaatioOrder sortBy,
            @Parameter(description = "Järjestyksen suunta") @RequestParam(required = false, defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) Long offset,
            @RequestParam(required = false, defaultValue = "20") Long amount) {
        return kutsuService.listKutsus(sortBy, direction, kutsuCriteria, offset, amount);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Uuden kutsun luominen. Vaatii samat oikeudet kuin uuden käyttöoikeuden myöntäminen.")
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_KUTSU_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    public ResponseEntity<Long> create(@Validated @RequestBody KutsuCreateDto kutsu) {
        long id = kutsuService.createKutsu(kutsu);
        URI location = fromCurrentRequestUri().pathSegment(String.valueOf(id)).build().toUri();
        return ResponseEntity.created(location).body(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_KUTSU_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    public void delete(@PathVariable Long id) {
        kutsuService.deleteKutsu(id);
    }

    @PutMapping(value = "/{id}/renew", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Kutsun uusiminen muuttamatta kutsun sisältöä eikä uusimisesta jää tietoa")
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_KUTSU_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    public void renew(@PathVariable Long id) {
        kutsuService.renewKutsu(id);
    }

}
