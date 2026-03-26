package fi.vm.sade.auth.cas;

import java.util.Optional;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.pac4j.core.client.BaseClient;

import fi.vm.sade.auth.clients.KayttooikeusClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OtuvaDelegatedAuthenticationProcessor implements DelegatedAuthenticationPreProcessor {
    final PrincipalFactory principalFactory;
    final KayttooikeusClient kayttooikeusClient;

    @Override
    public Principal process(Principal principal, BaseClient client, Credential credential, Service service)
            throws Throwable {
        try {
            var userAttributes = getUserAttributes(client, principal);
            var casPrincipal = CasPrincipal.of(principalFactory, userAttributes);
            LOGGER.info("Delegated authentication processing principal [{}] returned [{}]", principal, casPrincipal);
            return casPrincipal;
        } catch (Exception e) {
            LOGGER.error("failed to process delegated authentication (client " + client.getName() + ") for principal " + principal.getId(), e);
            throw new PreventedException(e);
        }
    }

    CasUserAttributes getUserAttributes(BaseClient client, Principal principal) {
        var a = switch (client.getName()) {
            case "mpassid" -> kayttooikeusClient.getUserAttributesByOid(principal.getId());
            case "haka" -> kayttooikeusClient.getUserAttributesByIdpIdentifier(client.getName(), (String) principal.getAttributes().get("urn:oid:1.3.6.1.4.1.5923.1.1.1.6").get(0));
            case "suomifi" -> kayttooikeusClient.getUserAttributesByHetu((String) principal.getAttributes().get("urn:oid:1.2.246.21").get(0));
            default -> null;
        };
        if (isHakaIdp(client) && a == null) {
            a = kayttooikeusClient.getUserAttributesByIdpIdentifier("haka", (String) principal.getAttributes().get("urn:oid:1.3.6.1.4.1.5923.1.1.1.6").get(0));
        }
        if (a == null) {
            throw new PreventedException("invalid delegated authentication client (" + client.getName() + ") for principal " + principal.getId());
        }
        return new CasUserAttributes(
            a.oidHenkilo(),
            a.username(),
            principal.getAttributes().get("sessionindex"),
            a.mfaProvider(),
            a.kayttajaTyyppi(),
            getIdpEntityId(client),
            a.roles()
        );
    }

    boolean isHakaIdp(BaseClient client) {
        return client.getName().equals("haka") || client.getName().startsWith("haka-");
    }

    Optional<String> getIdpEntityId(BaseClient client) {
        return "suomifi".equals(client.getName()) ? Optional.of("vetuma") :  Optional.of(client.getName());
    }
}
