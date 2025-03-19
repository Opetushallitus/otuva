package fi.vm.sade.auth.cas;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.pac4j.core.client.BaseClient;

import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DelegatedAuthenticationProcessor implements DelegatedAuthenticationPreProcessor {
    final PrincipalFactory principalFactory;
    final KayttooikeusRestClient kayttooikeusRestClient;

    @Override
    public Principal process(Principal principal, BaseClient client, Credential credential, Service service)
            throws Throwable {
        try {
            var userAttributes = getUserAttributes(client, principal);
            var casPrincipal = CasPrincipal.of(principalFactory, userAttributes);
            return casPrincipal;
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
