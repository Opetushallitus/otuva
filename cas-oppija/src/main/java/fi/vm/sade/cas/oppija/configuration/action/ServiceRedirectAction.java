package fi.vm.sade.cas.oppija.configuration.action;

import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.Client;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.profile.SAML2Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT;

public class ServiceRedirectAction extends AbstractServiceParamAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRedirectAction.class);

    private final Pac4jClientProvider clientProvider;

    public ServiceRedirectAction(Pac4jClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    protected Event doExecute(RequestContext context) {
        SAML2Profile profile = context.getRequestScope().get(REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT, SAML2Profile.class);
        Client<?, ?> client = clientProvider.getClient(profile);
        if (client instanceof SAML2Client) {
            return null; // SAML logout kesken, ei aseteta redirectiä!
        }

        var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        var service = getServiceRedirectCookie(request);
        if (service != null) {
            LOGGER.debug("Found service redirect cookie, setting logout redirect url: " + service);
            WebUtils.putLogoutRedirectUrl(context, service);
            clearServiceRedirectCookie(response);
        }
        return null;
    }

}
