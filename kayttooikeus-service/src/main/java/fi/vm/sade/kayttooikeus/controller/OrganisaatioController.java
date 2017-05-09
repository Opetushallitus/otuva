package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.service.OrganisaatioService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/organisaatio")
@RequiredArgsConstructor
public class OrganisaatioController {

    private final OrganisaatioService organisaatioService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation("Päivittää organisaatiovälimuistin.")
    public synchronized void updateCache() {
        organisaatioService.updateOrganisaatioCache();
    }

}
