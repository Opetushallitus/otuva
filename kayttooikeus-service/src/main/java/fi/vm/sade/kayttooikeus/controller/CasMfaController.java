package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.config.properties.CasProperties;
import fi.vm.sade.kayttooikeus.dto.CasGoogleAuthToken;
import fi.vm.sade.kayttooikeus.dto.MfaTriggerDto;
import fi.vm.sade.kayttooikeus.model.GoogleAuthToken;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import lombok.RequiredArgsConstructor;

import org.apache.commons.codec.binary.Base64;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "/mfa")
@RequiredArgsConstructor
public class CasMfaController {
    private static final Logger logger = LoggerFactory.getLogger(CasMfaController.class);
    private final KayttajatiedotService kayttajatiedotService;
    private final CasProperties casProperties;

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

    private String encryptAndSign(String payload) throws JoseException {
        var keys = new HashMap<String, Object>(2);
        keys.put("kty", "oct");
        keys.put("k", casProperties.getMfa().getEncryptionKey());
        var key = JsonWebKey.Factory.newJwk(keys).getKey();

        var jwe = new JsonWebEncryption();
        jwe.setPayload(payload);
        jwe.enableDefaultCompression();
        jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.DIRECT);
        jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        jwe.setKey(key);
        jwe.setContentTypeHeaderValue("JWT");
        jwe.setHeader("typ", "JWT");
        var encodedSecretKey = jwe.getCompactSerialization();

        var signingKey = new AesKey(casProperties.getMfa().getSigningKey().getBytes(StandardCharsets.UTF_8));

        var bytez = encodedSecretKey.getBytes(StandardCharsets.UTF_8);
        var base64 = Base64.encodeBase64URLSafeString(bytez);
        var jws = new JsonWebSignature();
        jws.setEncodedPayload(base64);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512);
        jws.setKey(signingKey);
        jws.setHeader("typ", "JWT");
        var secretKey = jws.getCompactSerialization();

        return secretKey;
    }

    // maps a MFA token to something that CAS can deserialize
    private Object mapGoogleAuthTokenToCas(GoogleAuthToken dto, String username) {
        try {
            var token = new CasGoogleAuthToken();
            token.setId(dto.getId());
            token.setUsername(username);
            token.setValidationCode(dto.getValidationCode());
            token.setScratchCodes(List.of("java.util.ArrayList", List.of(dto.getScratchCodes())));
            token.setRegistrationDate(dto.getRegistrationDate().toString() + "Z");
            token.setName(dto.getName());
            token.setSecretKey(encryptAndSign(dto.getSecretKey()));
            return List.of("java.util.ArrayList", List.of(token));
        } catch (Exception e) {
            logger.error("Error while creating a Google Auth response", e);
            return null;
        }
    }

    @GetMapping(value = "/token", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole(" +
            "'ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_READ', " +
            "'ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_CRUD')")
    public Object getGoogleAuthToken(HttpServletRequest request, HttpServletResponse response) {
        var username = request.getHeader("username");
        return kayttajatiedotService
          .getGoogleAuthToken(username)
          .map(t -> mapGoogleAuthTokenToCas(t, username))
          .orElseThrow();
    }
}
