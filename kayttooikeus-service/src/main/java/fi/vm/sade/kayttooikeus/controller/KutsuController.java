package fi.vm.sade.kayttooikeus.controller;

import com.querydsl.core.types.Order;
import fi.vm.sade.kayttooikeus.dto.KutsuListDto;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.querydsl.core.types.Order.DESC;
import fi.vm.sade.kayttooikeus.dto.KutsuDto;
import static fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder.AIKALEIMA;
import static fi.vm.sade.kayttooikeus.repositories.OrderBy.orderer;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    public List<KutsuListDto> listAvoinKutsus(
            @ApiParam("Järjestysperuste") @RequestParam(required = false) KutsuOrganisaatioOrder sortBy,
            @ApiParam("Järjestyksen suunta") @RequestParam(required = false) Order direction) {
        return kutsuService.listAvoinKutsus(orderer(sortBy, direction).byDefault(AIKALEIMA, DESC));
    }

    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    public ResponseEntity<KutsuDto> create(@Validated @RequestBody KutsuDto kutsu) {
        kutsu = kutsuService.createKutsu(kutsu);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .pathSegment(String.valueOf(kutsu.getId()))
                .build().toUri();
        return ResponseEntity.created(location).body(kutsu);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    public KutsuDto read(@PathVariable Long id) {
        return kutsuService.getKutsu(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    public void delete(@PathVariable Long id) {
        kutsuService.deleteKutsu(id);
    }
}
