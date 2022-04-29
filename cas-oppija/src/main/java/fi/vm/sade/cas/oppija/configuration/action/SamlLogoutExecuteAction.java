
package fi.vm.sade.cas.oppija.configuration.action;
/*
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.Client;
import org.pac4j.core.redirect.RedirectAction;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.profile.SAML2Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT;

*/
/**
 * SAML logout action. Expects SAML profile to be in request scope ({@link SamlLogoutPrepareAction}).
 * Implementation is mostly derived from DelegatedAuthenticationSAML2ClientLogoutAction.
 *//*

public class SamlLogoutExecuteAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlLogoutExecuteAction.class);

    private final Pac4jClientProvider clientProvider;

    public SamlLogoutExecuteAction(Pac4jClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            var context = Pac4jUtils.getPac4jJ2EContext(request, response);
            SAML2Profile profile = requestContext.getRequestScope().get(REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT, SAML2Profile.class);
            Client<?, ?> client = clientProvider.getClient(profile);
            if (client instanceof SAML2Client) {
                var saml2Client = (SAML2Client) client;
                LOGGER.debug("Located SAML2 client [{}]", saml2Client);
                var action = saml2Client.getLogoutAction(context, profile, null);
                LOGGER.debug("Preparing logout message to send is [{}]", action.getLocation());
                return handleLogout(action, requestContext);
            } else {
                LOGGER.debug("The current client is not a SAML2 client or it cannot be found at all, no logout action will be executed.");
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return null;
    }

    protected Event handleLogout(RedirectAction action, RequestContext context) {
        switch (action.getType()) {
            case REDIRECT:
                WebUtils.putLogoutRedirectUrl(context, action.getLocation());
                return null;
            default:
                throw new IllegalArgumentException("Unhandled logout request code: " + action.getType());
        }
    }

}
*/
