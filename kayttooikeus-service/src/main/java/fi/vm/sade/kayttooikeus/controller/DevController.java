package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
public class DevController {

    private final LdapSynchronizationService ldapSynchronizationService;

    @PutMapping("/ldapsynkronointi")
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_SCHEDULE',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation("Suorittaa LDAP-synkronoinnin")
    public void ldapSynkronointi() {
        ldapSynchronizationService.runSynchronizer();
    }

}
