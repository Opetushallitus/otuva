package fi.vm.sade.auth.cas;

import com.google.gson.Gson;
import fi.vm.sade.auth.Json;
import fi.vm.sade.auth.clients.KayttooikeusClient;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import lombok.Data;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static java.util.Collections.emptyList;

public class HttpAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private final OphHttpClient httpClient;
    private final KayttooikeusClient kayttooikeusClient;
    private final Gson gson;

    @Value("${oauth2.enabled}")
    private boolean oauth2Enabled;

    public HttpAuthenticationHandler(Integer order, OphHttpClient httpClient, KayttooikeusClient kayttooikeusClient) {
        this(new DefaultPrincipalFactory(), order, httpClient, kayttooikeusClient, new Gson());
    }

    public HttpAuthenticationHandler(PrincipalFactory principalFactory, Integer order, OphHttpClient httpClient, KayttooikeusClient kayttooikeusClient, Gson gson) {
        super("HttpAuthenticationHandler", principalFactory, order);
        this.httpClient = httpClient;
        this.kayttooikeusClient = kayttooikeusClient;
        this.gson = gson;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(UsernamePasswordCredential credential, String originalPassword) throws GeneralSecurityException, PreventedException {
        Optional<CasUserAttributes> opt;
        try {
            opt = oauth2Enabled
                ? kayttooikeusClient.getUserAttributesByUsernamePassword(credential.getUsername(), String.valueOf(credential.getPassword()))
                : validateUsernamePassword(credential.getUsername(), String.valueOf(credential.getPassword()));
        } catch (Exception e) {
            throw new PreventedException(e);
        }
        var userAttributes = opt.orElseThrow(() -> new FailedLoginException("Invalid credentials"));
        var principal = CasPrincipal.of(principalFactory, userAttributes);
        return createHandlerResult(credential, principal, emptyList());

    }

    private Optional<CasUserAttributes> validateUsernamePassword(String username, String password) {
        return httpClient.post("kayttooikeus-service.user-details")
                .retryOnError(3)
                .dataWriter("application/json", "UTF-8", out
                        -> gson.toJson(new LoginDto(username, password), out))
                .expectStatus(200, 401)
                .execute(handler -> {
                    if (handler.getStatusCode() == 401) {
                        return Optional.empty();
                    }
                    return Optional.of(Json.parse(handler.asText(), CasUserAttributes.class));
                });
    }

    private record LoginDto(String username, String password) {}

    @Data
    private static class KayttajatiedotReadDto {
        private final String username;
        private final String mfaProvider;
        private final String kayttajaTyyppi;
    }
}
