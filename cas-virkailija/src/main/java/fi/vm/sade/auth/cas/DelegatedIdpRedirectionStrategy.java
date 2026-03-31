package fi.vm.sade.auth.cas;

import java.util.Optional;
import java.util.Set;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.springframework.webflow.execution.RequestContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DelegatedIdpRedirectionStrategy implements DelegatedClientIdentityProviderRedirectionStrategy {
    @Override
    public Optional<DelegatedClientIdentityProviderConfiguration> select(RequestContext context,
            WebApplicationService service, Set<DelegatedClientIdentityProviderConfiguration> provider)
            throws Throwable {
        if (service.getOriginalUrl().contains("virkailijaRegistrationToken")) {
            return provider.stream()
                    .filter(idp -> idp.getName().equals("suomifi"))
                    .findFirst()
                    .map(idp -> {
                        idp.setAutoRedirectType(DelegationAutoRedirectTypes.SERVER);
                        return idp;
                    });
        } else {
            return Optional.empty();
        }
    }
}
