package fi.vm.sade.auth.cas;

import java.util.Optional;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.pac4j.core.client.BaseClient;

import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DelegatedAuthenticationProcessor implements DelegatedAuthenticationPreProcessor {
    final PrincipalFactory principalFactory;
    final KayttooikeusRestClient kayttooikeusRestClient;

    @Override
    public Principal process(Principal principal, BaseClient client, Credential credential, Service service)
            throws Throwable {
        try {
            if (isHakaRegistration(client, service)) {
                var hakaRegistrationPrincipal = CasPrincipal.hakaRegistrationPrincipalOf(principalFactory, principal, service);
                LOGGER.info("Haka registration for principal [{}]", hakaRegistrationPrincipal);
                return hakaRegistrationPrincipal;
            }

            var userAttributes = getUserAttributes(client, principal);
            var casPrincipal = CasPrincipal.of(principalFactory, userAttributes);
            LOGGER.info("Delegated authentication processing principal [{}] returned [{}]", principal, casPrincipal);
            return casPrincipal;
        } catch (Exception e) {
            throw new PreventedException(e);
        }
    }

    private static boolean isHakaRegistration(BaseClient client, Service service) {
        return "haka".equals(client.getName()) && service.getOriginalUrl().contains("hakaRegistrationTemporaryToken");
    }

    CasUserAttributes getUserAttributes(BaseClient client, Principal principal) {
        var a = switch (client.getName()) {
            case "mpassid" -> kayttooikeusRestClient.getUserAttributesByOid(principal.getId());
            case "haka" -> kayttooikeusRestClient.getUserAttributesByIdpIdentifier(client.getName(), (String) principal.getAttributes().get("urn:oid:1.3.6.1.4.1.5923.1.1.1.6").get(0));
            default -> {
                throw new PreventedException("invalid delegated authentication client (" + client.getName() + ") for principal " + principal.getId());
            }
        };
        return new CasUserAttributes(a.oidHenkilo(), a.username(), a.mfaProvider(), a.kayttajaTyyppi(), Optional.of(client.getName()), a.roles());
    }
}
