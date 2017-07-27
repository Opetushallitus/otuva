package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto;
import fi.vm.sade.kayttooikeus.dto.KutsuReadDto;
import fi.vm.sade.kayttooikeus.enumeration.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

@RestController
@RequestMapping("/kutsu")
@Api(value = "/kutsu", description = "Virkailijan kutsumiseen liittyvät toiminnot")
public class KutsuController {
    private final KutsuService kutsuService;
    
    @Autowired
    public KutsuController(KutsuService kutsuService) {
        this.kutsuService = kutsuService;
    }

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ApiOperation("Hakee omat avoimet kutsut.")
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    public List<KutsuReadDto> listAvoinKutsus(
            @ApiParam("Järjestysperuste") @RequestParam(required = false, defaultValue = "AIKALEIMA") KutsuOrganisaatioOrder sortBy,
            @ApiParam("Järjestyksen suunta") @RequestParam(required = false) Sort.Direction direction,
            @RequestParam(required = false, defaultValue = "true") Boolean onlyOwnKutsus) {
        return kutsuService.listAvoinKutsus(sortBy, direction, onlyOwnKutsus);
    }

    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    public ResponseEntity<Long> create(@Validated @RequestBody KutsuCreateDto kutsu) {
        long id = kutsuService.createKutsu(kutsu);
        URI location = fromCurrentRequestUri().pathSegment(String.valueOf(id)).build().toUri();
        return ResponseEntity.created(location).body(id);
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    public KutsuReadDto read(@PathVariable Long id) {
        return kutsuService.getKutsu(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    public void delete(@PathVariable Long id) {
        kutsuService.deleteKutsu(id);
    }

    // Consumes temporary tokens so not authenticated
    @RequestMapping(value = "/token/{temporaryToken}", method = RequestMethod.GET)
    public KutsuReadDto getByToken(@PathVariable String temporaryToken) {
        return this.kutsuService.getByTemporaryToken(temporaryToken);
    }
}
