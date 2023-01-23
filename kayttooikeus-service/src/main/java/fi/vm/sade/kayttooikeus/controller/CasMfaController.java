package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.CasGoogleAuthToken;
import fi.vm.sade.kayttooikeus.dto.MfaTriggerDto;
import fi.vm.sade.kayttooikeus.model.GoogleAuthToken;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "/mfa")
@RequiredArgsConstructor
public class CasMfaController {
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

    // maps a MFA token to something that CAS can deserialize
    private Object mapGoogleAuthTokenToCas(GoogleAuthToken dto, String username) {
        var token = new CasGoogleAuthToken();
        token.setId(dto.getId());
        token.setUsername(username);
        token.setValidationCode(dto.getValidationCode());
        token.setScratchCodes(List.of("java.util.ArrayList", List.of(dto.getScratchCodes())));
        token.setRegistrationDate(dto.getRegistrationDate().toString());
        token.setName(dto.getName());
        token.setSecretKey(dto.getSecretKey());
        return List.of("java.util.ArrayList", List.of(token));
    }

    @GetMapping(value = "/token", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole(" +
            "'ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_READ', " +
            "'ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_CRUD')")
    public void getGoogleAuthToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var username = request.getHeader("username");
        var token = kayttajatiedotService
          .getGoogleAuthToken(username)
          .map(t -> mapGoogleAuthTokenToCas(t, username))
          .orElseThrow();
        ObjectMapper mapper = new ObjectMapper();
        String serializedJson = mapper.writeValueAsString(token);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(serializedJson);
        response.getWriter().flush();
    }
}
