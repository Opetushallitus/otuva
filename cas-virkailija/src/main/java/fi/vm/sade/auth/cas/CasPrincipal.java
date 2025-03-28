package fi.vm.sade.auth.cas;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;

import java.util.List;
import java.util.Map;

public class CasPrincipal {
    public static Principal of(PrincipalFactory principalFactory, CasUserAttributes userAttributes) {
        try {
            Map<String, List<Object>> attributes = Map.of(
                    "oidHenkilo", List.of(userAttributes.oidHenkilo()),
                    "idpEntityId", List.of(userAttributes.idpEntityId().orElse("usernamePassword")),
                    "kayttajaTyyppi", List.of(userAttributes.kayttajaTyyppi().orElse("VIRKAILIJA")),
                    "roles", List.copyOf(userAttributes.roles())
            );
            return principalFactory.createPrincipal(userAttributes.username(), attributes);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static Principal hakaRegistrationPrincipalOf(PrincipalFactory principalFactory, Principal hakaPrincipal, Service service) {
        try {
            String identifier = (String) hakaPrincipal.getAttributes().get("urn:oid:1.3.6.1.4.1.5923.1.1.1.6").get(0);
            String[] splitUrl = service.getOriginalUrl().split("/");
            return principalFactory.createPrincipal("<<hakaRegistrationPrincipal>>", Map.of(
                    "identifier", List.of(identifier),
                    "temporaryToken", List.of(splitUrl[splitUrl.length - 1])));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
