package fi.vm.sade.kayttooikeus.controller;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fi.vm.sade.kayttooikeus.dto.GoogleAuthSetupDto;
import fi.vm.sade.kayttooikeus.service.MfaService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/mfa", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "MFA API")
@RequiredArgsConstructor
public class MfaSetupController {
    private final MfaService mfaService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/gauth/setup")
    public GoogleAuthSetupDto setupMfa() {
      return mfaService.setupGoogleAuth();
    }
}
