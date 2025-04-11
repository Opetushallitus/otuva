package fi.vm.sade.auth.discovery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
public class SamlDiscoveryAction extends BaseCasWebflowAction {
    protected final CasConfigurationProperties casProperties;
    protected final DelegatedClientAuthenticationConfigurationContext configContext;

    /**
     * @param requestContext
     * @return
     * @throws Exception
     */
    @Override
    protected Event doExecuteInternal(RequestContext requestContext) throws Exception {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);

        val webContext = new JEEContext(request, response);
        val clientName = configContext.getDelegatedClientNameExtractor().extract(webContext)
                .orElseGet(() -> (String) request.getAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER));

        String selectedIdP = request.getParameter("entityID");

        if(selectedIdP != null) {
            LOGGER.debug("Discovery service seems to have chosen IdP [{}]", selectedIdP);
            return new Event(this, SamlDiscoveryWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_SUCCESS);
        }
        val samlProperties = getClientProperties(clientName);
        if(samlProperties.isEmpty()) {
            LOGGER.info("No discovery configuration for [{}], going on with redirection flow", clientName);
            return new Event(this, SamlDiscoveryWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_SUCCESS);
        }
        val client = getClient(clientName);
        if(client == null) {
            LOGGER.warn("No client with name [{}] found for SAML2 discovery", clientName);
            return new Event(this, SamlDiscoveryWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_SUCCESS);
        }
        val discoveryUrl = samlProperties.get().getDiscoveryServiceUrl();
        if( discoveryUrl == null || String.valueOf(discoveryUrl).isEmpty()) {
            LOGGER.warn("No discovery URL for client [{}] found", clientName);
            return new Event(this, SamlDiscoveryWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_SUCCESS);
        }
        val requestScope = requestContext.getRequestScope();
        // set request scope variables for the redirect view action
        requestScope.put(
                SamlDiscoveryWebflowConstants.VAR_ID_DELEGATED_AUTHENTICATION_DISCOVERY_URL,
                samlProperties.get().getDiscoveryServiceUrl()
                );
        requestScope.put(
                SamlDiscoveryWebflowConstants.VAR_ID_DELEGATED_AUTHENTICATION_CLIENT_NAME,
                clientName
        );
        requestScope.put(
                SamlDiscoveryWebflowConstants.VAR_ID_DELEGATED_AUTHENTICATION_ENTITY_ID,
                client.getServiceProviderResolvedEntityId()
        );
        return new Event(this, SamlDiscoveryWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_REDIRECT);
    }

    private Optional<Pac4jSamlClientProperties> getClientProperties(String name) {
        return casProperties.getAuthn().getPac4j().getSaml()
                .stream()
                .filter(saml -> saml.getClientName().equals(name))
                .findFirst();
    }

    private SAML2Client getClient(String clientName) {
        val client = configContext.getIdentityProviders().findClient(clientName);
        if(client.isEmpty() || !(client.get() instanceof SAML2Client)) {
            return null;
        }
        return (SAML2Client) client.get();
    }
}
