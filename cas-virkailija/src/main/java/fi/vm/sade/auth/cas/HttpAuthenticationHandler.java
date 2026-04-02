package fi.vm.sade.auth.cas;

import fi.vm.sade.auth.clients.KayttooikeusClient;
import lombok.Data;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static java.util.Collections.emptyList;

public class HttpAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private final KayttooikeusClient kayttooikeusClient;

    public HttpAuthenticationHandler(Integer order, KayttooikeusClient kayttooikeusClient) {
        this(new DefaultPrincipalFactory(), order, kayttooikeusClient);
    }

    public HttpAuthenticationHandler(PrincipalFactory principalFactory, Integer order, KayttooikeusClient kayttooikeusClient) {
        super("HttpAuthenticationHandler", principalFactory, order);
        this.kayttooikeusClient = kayttooikeusClient;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(UsernamePasswordCredential credential, String originalPassword) throws GeneralSecurityException, PreventedException {
        Optional<CasUserAttributes> opt;
        try {
            opt = kayttooikeusClient.getUserAttributesByUsernamePassword(credential.getUsername(), String.valueOf(credential.getPassword()));
        } catch (Exception e) {
            throw new PreventedException(e);
        }
        var userAttributes = opt.orElseThrow(() -> new FailedLoginException("Invalid credentials"));
        var principal = CasPrincipal.of(principalFactory, userAttributes);
        return createHandlerResult(credential, principal, emptyList());
    }

    @Data
    private static class KayttajatiedotReadDto {
        private final String username;
        private final String mfaProvider;
        private final String kayttajaTyyppi;
    }
}
