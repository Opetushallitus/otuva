package fi.vm.sade.auth.cas;

import java.util.Optional;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.pac4j.core.client.BaseClient;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import fi.vm.sade.auth.clients.KayttooikeusClient;
import fi.vm.sade.auth.clients.VirkailijaRegistration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OtuvaDelegatedAuthenticationProcessor implements DelegatedAuthenticationPreProcessor {
    final PrincipalFactory principalFactory;
    final KayttooikeusClient kayttooikeusClient;
    final boolean registrationEnabled;
    final boolean registrationTestSuomifi;

    @Override
    public Principal process(Principal principal, BaseClient client, Credential credential, Service service)
            throws Throwable {
        try {
            var registrationToken = registrationEnabled ? getRegistrationToken(service) : null;
            var userAttributes = registrationToken != null && client.getName().equals("suomifi")
                ? registerVirkailija(principal, client, registrationToken)
                : getUserAttributes(client, principal);
            var casPrincipal = CasPrincipal.of(principalFactory, userAttributes);
            LOGGER.info("Delegated authentication processing principal [{}] returned [{}]", principal, casPrincipal);
            return casPrincipal;
        } catch (Exception e) {
            LOGGER.error("failed to process delegated authentication (client " + client.getName() + ") for principal " + principal.getId(), e);
            throw new PreventedException(e);
        }
    }

    String getRegistrationToken(Service service) {
        try {
            var uri = UriComponentsBuilder.fromUriString(service.getOriginalUrl()).build();
            var registrationToken = uri.getQueryParams().getFirst("virkailijaRegistrationToken");
            LOGGER.info("Parsed token " + registrationToken + " from service url " + service.getOriginalUrl());
            return registrationToken;
        } catch (Exception e) {
            LOGGER.info("Failed to parse registration token from url " + service.getOriginalUrl());
            return null;
        }
    }

    CasUserAttributes registerVirkailija(Principal principal, BaseClient client, String registrationToken) {
        String etunimet = registrationTestSuomifi && !StringUtils.hasLength(getAttribute(principal, "givenName"))
                ? "Testi Etunimi"
                : getAttribute(principal, "givenName");
        String sukunimi = registrationTestSuomifi && !StringUtils.hasLength(getAttribute(principal, "sn"))
                ? "Testi-Sukunimi"
                : getAttribute(principal, "sn");
        var dto = new VirkailijaRegistration(
            registrationToken,
            getAttribute(principal, "urn:oid:1.2.246.21"),
            etunimet,
            sukunimi);
        LOGGER.info("Registering virkailija [{}]", dto);
        return kayttooikeusClient.registerVirkailija(dto);
    }

    CasUserAttributes getUserAttributes(BaseClient client, Principal principal) {
        var a = switch (client.getName()) {
            case "mpassid" -> kayttooikeusClient.getUserAttributesByOid(principal.getId());
            case "haka" -> kayttooikeusClient.getUserAttributesByIdpIdentifier(client.getName(), getAttribute(principal, "urn:oid:1.3.6.1.4.1.5923.1.1.1.6"));
            case "suomifi" -> kayttooikeusClient.getUserAttributesByHetu(getAttribute(principal, "urn:oid:1.2.246.21"));
            default -> null;
        };
        if (isHakaIdp(client) && a == null) {
            a = kayttooikeusClient.getUserAttributesByIdpIdentifier("haka", getAttribute(principal, "urn:oid:1.3.6.1.4.1.5923.1.1.1.6"));
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

    String getAttribute(Principal principal, String attr) {
        var attrList = principal.getAttributes().get(attr);
        if (attrList != null && attrList.size() > 0) {
            return (String) attrList.get(0);
        } else {
            return null;
        }
    }
}
