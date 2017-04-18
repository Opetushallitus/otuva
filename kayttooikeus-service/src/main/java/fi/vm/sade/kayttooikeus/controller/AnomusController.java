package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.HaettuKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.dto.UpdateHaettuKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.service.KayttooikeusAnomusService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
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

    @ApiOperation("Palauttaa henkilön kaikki haetut käyttöoikeusryhmät")
    @PostAuthorize("@permissionCheckerServiceImpl.hasRoleForOrganisations(returnObject, {'READ', 'READ_UPDATE', 'CRUD'})")
    @RequestMapping(value = "/{oidHenkilo}", method = RequestMethod.GET)
    public List<HaettuKayttooikeusryhmaDto> getActiveAnomuksetByHenkilo(@ApiParam("Henkilön OID") @PathVariable String oidHenkilo,
                                                                        @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        return this.kayttooikeusAnomusService.getAllActiveAnomusByHenkiloOid(oidHenkilo, activeOnly);
    }

    @ApiOperation("Hyväksyy tai hylkää haetun käyttöoikeusryhmän")
    // Organisation access validated on server layer
    @PreAuthorize("hasAnyRole('ROLE_APP_ANOMUSTENHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public void updateHaettuKayttooikeusryhma(@RequestBody @Validated UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto) {
        this.kayttooikeusAnomusService.updateHaettuKayttooikeusryhma(updateHaettuKayttooikeusryhmaDto);
    }

}
