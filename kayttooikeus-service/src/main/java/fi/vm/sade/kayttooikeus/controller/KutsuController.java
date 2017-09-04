package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.KutsuUpdateDto;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkiloCreateByKutsuDto;
import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto;
import fi.vm.sade.kayttooikeus.dto.KutsuReadDto;
import fi.vm.sade.kayttooikeus.enumeration.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.repositories.criteria.KutsuCriteria;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
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
@Api(tags = "Virkailijan kutsumiseen liittyvät toiminnot")
public class KutsuController {
    private final KutsuService kutsuService;
    
    @Autowired
    public KutsuController(KutsuService kutsuService) {
        this.kutsuService = kutsuService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ApiOperation("Hakee kutsut annettujen hakuehtojen perusteella.")
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    public List<KutsuReadDto> listAvoinKutsus(
            KutsuCriteria kutsuCriteria,
            @ApiParam("Järjestysperuste") @RequestParam(required = false, defaultValue = "AIKALEIMA") KutsuOrganisaatioOrder sortBy,
            @ApiParam("Järjestyksen suunta") @RequestParam(required = false, defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) Long offset,
            @RequestParam(required = false, defaultValue = "20") Long amount) {
        return kutsuService.listKutsus(sortBy, direction, kutsuCriteria, offset, amount);
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

    @RequestMapping(value = "/{temporaryToken}/token/identifier", method = RequestMethod.PUT)
    @ApiOperation("Kutsun päivittäminen väliaikaisella tokenilla. Sallii osittaisen päivittämisen.")
    @PreAuthorize("hasRole('ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    public void updateIdentifierByToken(@PathVariable String temporaryToken,
                                        @RequestBody KutsuUpdateDto kutsuUpdateDto) {
        this.kutsuService.updateHakaIdentifierToKutsu(temporaryToken, kutsuUpdateDto);
    }

    /**
     *  /kutsu is open to non-authenticated use.
     */

    // Uses temporary tokens so not authenticated
    @ApiOperation("Get kutsu by temporary token")
    @RequestMapping(value = "/token/{temporaryToken}", method = RequestMethod.GET)
    public KutsuReadDto getByToken(@PathVariable String temporaryToken) {
        return this.kutsuService.getByTemporaryToken(temporaryToken);
    }

    // Consumes single use temporary tokens so not authenticated
    @ApiOperation("Luo henkilön väliaikaisella tokenilla. Palauttaa authTokenin kirjautumista varten.")
    @RequestMapping(value = "/token/{temporaryToken}", method = RequestMethod.POST)
    public String createByToken(@PathVariable String temporaryToken,
                                @Validated @RequestBody HenkiloCreateByKutsuDto henkiloCreateByKutsuDto) {
        return this.kutsuService.createHenkilo(temporaryToken, henkiloCreateByKutsuDto);
    }

}
