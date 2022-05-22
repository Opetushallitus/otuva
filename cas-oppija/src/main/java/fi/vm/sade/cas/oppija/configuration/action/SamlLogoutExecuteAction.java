
package fi.vm.sade.cas.oppija.configuration.action;

import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.profile.SAML2Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT;

/**
 * SAML logout action. Expects SAML profile to be in request scope ({@link SamlLogoutPrepareAction}).
 * Implementation is mostly derived from DelegatedAuthenticationSAML2ClientLogoutAction.
 */
public class SamlLogoutExecuteAction extends AbstractAction {

    private static final Logger SLF4J_LOGGER = LoggerFactory.getLogger(SamlLogoutExecuteAction.class);

    private final Pac4jClientProvider clientProvider;
    private final SessionStore sessionStore;

    public SamlLogoutExecuteAction(Pac4jClientProvider clientProvider, SessionStore sessionStore) {
        this.clientProvider = clientProvider;
        this.sessionStore = sessionStore;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            //var context = WebUtils.getPac4jJ2EContext(request, response);
            var context = new JEEContext(request, response); // TODO Oikein??


            SAML2Profile profile = requestContext.getRequestScope().get(REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT, SAML2Profile.class);
            Client client = clientProvider.getClient(profile);
            if (client instanceof SAML2Client) {
                var saml2Client = (SAML2Client) client;
                SLF4J_LOGGER.debug("Located SAML2 client [{}]", saml2Client);
                final Optional<RedirectionAction> action = saml2Client.getLogoutAction(context, sessionStore, profile, null);

                if(action.isPresent()) {
                    //SLF4J_LOGGER.debug("Preparing logout message to send is [{}]", action.get().getLocation());
                    return handleLogout(action.get(), requestContext);
                }
            } else {
                SLF4J_LOGGER.debug("The current client is not a SAML2 client or it cannot be found at all, no logout action will be executed.");
            }
        } catch (final Exception e) {
            SLF4J_LOGGER.warn(e.getMessage(), e);
        }
        return null;
    }

    protected Event handleLogout(RedirectionAction action, RequestContext context) {
        if (action.getCode() == 302) {
            if (action instanceof FoundAction) {
                WebUtils.putLogoutRedirectUrl(context, ((FoundAction) action).getLocation());
                return null;
            }
        }
        throw new IllegalArgumentException("Unhandled logout request code: " + action.getCode());
    }

}

