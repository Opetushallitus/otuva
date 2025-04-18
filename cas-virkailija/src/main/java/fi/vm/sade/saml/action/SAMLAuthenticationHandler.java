package fi.vm.sade.saml.action;

import fi.vm.sade.auth.cas.CasPrincipal;
import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import org.apereo.cas.authentication.*;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public class SAMLAuthenticationHandler implements AuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger(SAMLAuthenticationHandler.class);

    private final PrincipalFactory principalFactory;
    private final Integer order;
    private final KayttooikeusRestClient kayttooikeusRestClient;

    public SAMLAuthenticationHandler(Integer order, KayttooikeusRestClient kayttooikeusRestClient) {
        this(new DefaultPrincipalFactory(), order, kayttooikeusRestClient);
    }

    public SAMLAuthenticationHandler(PrincipalFactory principalFactory, Integer order, KayttooikeusRestClient kayttooikeusRestClient) {
        this.principalFactory = requireNonNull(principalFactory);
        this.order = requireNonNull(order);
        this.kayttooikeusRestClient = requireNonNull(kayttooikeusRestClient);
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public boolean supports(Credential credentials) {
        return SAMLCredentials.class.isInstance(credentials);
    }

    @Override
    public boolean supports(Class<? extends Credential> clazz) {
        return SAMLCredentials.class.isAssignableFrom(clazz);
    }

    @Override
    public AuthenticationHandlerExecutionResult authenticate(Credential credential, Service service) throws GeneralSecurityException, PreventedException {
        try {
            return doAuthentication(credential);
        } catch (Exception e) {
            log.warn("WARNING - problem with authentication backend, using only ldap.", e);
            FailedLoginException fle = new FailedLoginException();
            fle.initCause(e);
            throw fle;
        }
    }

    private AuthenticationHandlerExecutionResult doAuthentication(Credential credential) {
        var userAttributes = kayttooikeusRestClient.getHenkiloByAuthToken(credential.getId());
        var principal = CasPrincipal.of(principalFactory, userAttributes);
        return new DefaultAuthenticationHandlerExecutionResult(this, credential, principal, emptyList());
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

}
