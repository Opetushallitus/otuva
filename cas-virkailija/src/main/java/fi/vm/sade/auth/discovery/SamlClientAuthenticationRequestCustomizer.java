package fi.vm.sade.auth.discovery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.webflow.execution.RequestContextHolder;

import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
public class SamlClientAuthenticationRequestCustomizer implements DelegatedClientAuthenticationRequestCustomizer {

    private final CasConfigurationProperties casProperties;

    /**
     * @param client
     * @param webContext
     */
    @Override
    public void customize(IndirectClient client, WebContext webContext) {
        val samlClient = (SAML2Client)client;
        val entity = RequestContextHolder.getRequestContext().getConversationScope()
                .get(SamlDiscoveryWebflowConstants.CONVERSATION_VAR_ID_DELEGATED_AUTHENTICATION_IDP,
                        SamlDiscoverySelectedIdP.class);

        if(entity != null && entity.getClientName().equals(client.getName())) {
            LOGGER.debug("Setting discovered identity provider entity id to [{}] for SAML2 client [{}]",
                    entity, client.getName());
            samlClient.getConfiguration().setIdentityProviderEntityId(entity.getEntityID());
            RequestContextHolder.getRequestContext().getConversationScope()
                    .remove(SamlDiscoveryWebflowConstants.CONVERSATION_VAR_ID_DELEGATED_AUTHENTICATION_IDP);
            return;
        }
        val samlProperties = getClientProperties(client.getName()).get();
        if(samlProperties.getIdentityProviderEntityId() != null &&
                !samlProperties.getIdentityProviderEntityId().isEmpty()) {
            LOGGER.debug("Setting identity provider entity id to [{}] for SAML2 client [{}]",
                    samlProperties.getIdentityProviderEntityId(), client.getName());
            samlClient.getConfiguration().setIdentityProviderEntityId(samlProperties.getIdentityProviderEntityId());
        }
    }

    /**
     * @param client
     * @param webContext
     * @return
     */
    @Override
    public boolean supports(IndirectClient client, WebContext webContext) {
        if(!(client instanceof SAML2Client)) {
            return false;
        }
        Optional<Pac4jSamlClientProperties> saml = getClientProperties(client.getName());
        if(saml.isEmpty()) {
            return false;
        }
        return saml.get().getDiscoveryServiceUrl() != null && !saml.get().getDiscoveryServiceUrl().isEmpty();
    }

    /**
     * @param webContext
     * @param client
     * @param currentService
     * @return
     */
    @Override
    public boolean isAuthorized(WebContext webContext, IndirectClient client, WebApplicationService currentService) {
        return true;
    }

    private Optional<Pac4jSamlClientProperties> getClientProperties(String name) {
        return casProperties.getAuthn().getPac4j().getSaml()
                .stream()
                .filter(saml -> saml.getClientName().equals(name))
                .findFirst();
    }

}
