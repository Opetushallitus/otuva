
package fi.vm.sade.cas.oppija.configuration.action;

import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.exception.TechnicalException;
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

    public SamlLogoutExecuteAction(Pac4jClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            var context = new JEEContext(request, response); // TODO Oikein??


            Client client;
            SAML2Profile profile = requestContext.getRequestScope().get(REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT, SAML2Profile.class);
            client = getClientOrNull(profile);

            if (client instanceof SAML2Client) {
                var saml2Client = (SAML2Client) client;
                SLF4J_LOGGER.debug("Located SAML2 client [{}]", saml2Client);
                final Optional<RedirectionAction> action = saml2Client.getLogoutAction(
                        context,
                        JEESessionStore.INSTANCE,
                        profile,
                        null
                );

                if (action.isPresent()) {
                    SLF4J_LOGGER.debug("Preparing logout message to send is [{}]", action);
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

    private Client getClientOrNull(SAML2Profile profile) {
        Client client;
        try {
            client = clientProvider.getClient(profile);
        } catch (final TechnicalException e) {
            SLF4J_LOGGER.debug("No SAML2 client found: " + e.getMessage(), e);
            client = null;
        }
        return client;
    }

    // TODO fix tshis
    protected Event handleLogout(RedirectionAction action, RequestContext context) {
        if (action.getCode() == 302 && action instanceof FoundAction) {
            WebUtils.putLogoutRedirectUrl(context, ((FoundAction) action).getLocation());
            return null;
        }
        throw new IllegalArgumentException("Unhandled logout request code: " + action.getCode() + " action of class:" + action.getClass());
    }

}

