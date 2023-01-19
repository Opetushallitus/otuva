package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.MfaTriggerDto;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "/mfa")
@RequiredArgsConstructor
public class MfaController {
    private final KayttajatiedotService kayttajatiedotService;

    @PostMapping(value = "/trigger", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole(" +
            "'ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_READ', " +
            "'ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_CRUD')")
    public void getMfaProvider(HttpServletResponse response, @Valid @RequestBody MfaTriggerDto dto) throws IOException {
        var mfaProvider = kayttajatiedotService
          .getMfaProvider(dto.getPrincipalId())
          .orElse("");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(mfaProvider);
        response.getWriter().flush();
    }
}
