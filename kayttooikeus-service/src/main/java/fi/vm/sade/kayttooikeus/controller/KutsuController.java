package fi.vm.sade.kayttooikeus.controller;

import com.querydsl.core.types.Order;
import fi.vm.sade.kayttooikeus.dto.KutsuListDto;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.querydsl.core.types.Order.DESC;
import static fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder.AIKALEIMA;
import static fi.vm.sade.kayttooikeus.repositories.OrderBy.orderer;

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
            @ApiParam("Järjestysperuste") @RequestParam(required = false) KutsuOrganisaatioOrder orderBy,
            @ApiParam("Järjestyksen suunta") @RequestParam(required = false) Order direction) {
        return kutsuService.listAvoinKutsus(orderer(orderBy, direction).byDefault(AIKALEIMA, DESC));
    }
}
