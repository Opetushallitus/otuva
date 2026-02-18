package fi.vm.sade;

import static fi.vm.sade.cas.oppija.CasOppijaUtils.resolveAttribute;

import java.util.List;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.pac4j.core.client.BaseClient;

import fi.vm.sade.client.OppijanumerorekisteriClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OtuvaDelegatedAuthenticationPreProcessor implements DelegatedAuthenticationPreProcessor {
    final PrincipalFactory principalFactory;
    final OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @Override
    public Principal process(Principal principal, BaseClient client, Credential credential, Service service)
            throws Throwable {
        try {
            String oid = resolveAttribute(principal.getAttributes(), "nationalIdentificationNumber", String.class)
                .map(oppijanumerorekisteriClient::getOidByHetu)
                .or(() -> resolveAttribute(principal.getAttributes(), "personIdentifier", String.class)
                        .map(oppijanumerorekisteriClient::getOidByEidas)
                )
                .orElse(null);
            principal.getAttributes().put("personOid", List.of(oid));
            LOGGER.debug("Delegated authentication processed principal [{}]", principal);
            return principal;
        } catch (Exception e) {
            LOGGER.error("failed to process delegated authentication for principal " + principal.getId(), e);
            throw new PreventedException(e);
        }
    }
}
