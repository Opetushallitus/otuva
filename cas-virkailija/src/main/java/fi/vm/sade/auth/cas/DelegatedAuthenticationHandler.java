package fi.vm.sade.auth.cas;

import java.util.ArrayList;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.provision.DelegatedClientUserProfileProvisioner;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jDelegatedAuthenticationCoreProperties;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.pac4j.authentication.handler.support.DelegatedClientAuthenticationHandler;
import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.context.ConfigurableApplicationContext;

import fi.vm.sade.auth.clients.KayttooikeusRestClient;

/**
 * Switches delegated authentication principal with CAS user attributes from kayttooikeus-service
 */
@Monitorable
@Slf4j
public class DelegatedAuthenticationHandler extends DelegatedClientAuthenticationHandler {
    final DelegatedIdentityProviders identityProviders;
    final PrincipalFactory principalFactory;
    final KayttooikeusRestClient kayttooikeusRestClient;

    public DelegatedAuthenticationHandler(
            final Pac4jDelegatedAuthenticationCoreProperties properties,
            final ServicesManager servicesManager,
            final PrincipalFactory principalFactory,
            final DelegatedIdentityProviders identityProviders,
            final DelegatedClientUserProfileProvisioner profileProvisioner,
            final SessionStore sessionStore,
            final ConfigurableApplicationContext applicationContext,
            final KayttooikeusRestClient kayttooikeusRestClient
    ) {
        super(properties, servicesManager, principalFactory, identityProviders, profileProvisioner, sessionStore, applicationContext);
        this.identityProviders = identityProviders;
        this.principalFactory = principalFactory;
        this.kayttooikeusRestClient = kayttooikeusRestClient;
    }

    @Override
    protected AuthenticationHandlerExecutionResult postFinalizeAuthenticationHandlerResult(
            final AuthenticationHandlerExecutionResult result,
            final ClientCredential credentials,
            final Principal principal,
            final BaseClient client,
            final Service service
    ) {
        try {
            var userAttributes = getUserAttributes(client, principal);
            var casPrincipal = CasPrincipal.of(principalFactory, userAttributes);
            return createHandlerResult(credentials, casPrincipal, new ArrayList<>(0));
        } catch (Exception e) {
            throw new PreventedException(e);
        }
    }

    CasUserAttributes getUserAttributes(BaseClient client, Principal principal) {
        if ("mpassid".equals(client.getName())) {
            return kayttooikeusRestClient.getHenkiloByOid(principal.getId());
        }
        throw new PreventedException("invalid delegated authentication client (" + client.getName() + ") for principal " + principal.getId());
    }
}
