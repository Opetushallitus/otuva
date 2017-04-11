package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.HaettuKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.service.KayttooikeusAnomusService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Kayttooikeusanomukset")
@RestController
@RequestMapping("/kayttooikeusanomus")
public class AnomusController {

    private final KayttooikeusAnomusService kayttooikeusAnomusService;

    @Autowired
    AnomusController(KayttooikeusAnomusService kayttooikeusAnomusService) {
        this.kayttooikeusAnomusService = kayttooikeusAnomusService;
    }

    @ApiOperation("Palauttaa henkilön kaikki aktiiviset anomukset")
    @PostAuthorize("@permissionCheckerServiceImpl.hasRoleForOrganisations(returnObject, {'READ', 'READ_UPDATE', 'CRUD'})")
    @RequestMapping(value = "/{oidHenkilo}", method = RequestMethod.GET)
    public List<HaettuKayttooikeusryhmaDto> getActiveAnomuksetByHenkilo(@ApiParam("Henkilön OID") @PathVariable String oidHenkilo,
                                                                        @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        return kayttooikeusAnomusService.getAllActiveAnomusByHenkiloOid(oidHenkilo, activeOnly);
    }

}
