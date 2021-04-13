package fi.vm.sade.cas.oppija.configuration.action;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.redirect.RedirectAction;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.profile.SAML2Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Arrays;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT;

/**
 * SAML logout action. Expects SAML profile to be in request scope ({@link SamlLogoutPrepareAction}).
 * Implementation is mostly derived from DelegatedAuthenticationSAML2ClientLogoutAction.
 */
public class SamlLogoutExecuteAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlLogoutExecuteAction.class);
    private static final String SERVICE_COOKIE = "_oph_service";
    /*
     * Keksin eliniän tarvitsee kattaa vain SAML-logout ja sitä seuraavat redirectit;
     * virkailijan tunnistautumisessa joka tapauksessa vain minuutin aikaikkuna.
     */
    private static final int SERVICE_COOKIE_MAX_AGE = 60;

    private final Clients clients;
    private final CasConfigurationProperties casProperties;

    public SamlLogoutExecuteAction(Clients clients, CasConfigurationProperties casProperties) {
        this.clients = clients;
        this.casProperties = casProperties;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            var context = Pac4jUtils.getPac4jJ2EContext(request, response);

            Client<?, ?> client;
            SAML2Profile profile = requestContext.getRequestScope().get(REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT, SAML2Profile.class);
            try {
                var currentClientName = profile == null ? null : profile.getClientName();
                client = currentClientName == null ? null : clients.findClient(currentClientName);
            } catch (final TechnicalException e) {
                LOGGER.debug("No SAML2 client found: " + e.getMessage(), e);
                client = null;
            }
            if (client instanceof SAML2Client) {
                var saml2Client = (SAML2Client) client;
                LOGGER.debug("Located SAML2 client [{}]", saml2Client);
                var action = saml2Client.getLogoutAction(context, profile, null);
                var service = getServiceRedirectParameter(request);
                if (service != null) {
                    setServiceRedirectCookie(response, service);
                    LOGGER.debug("Set service redirect cookie to value: " + service);
                }
                LOGGER.debug("Preparing logout message to send is [{}]", action.getLocation());
                return handleLogout(action, requestContext);
            } else {
                LOGGER.debug("The current client is not a SAML2 client or it cannot be found at all, no logout action will be executed.");
                /* SAML-logoutin jälkeen tehtävä CAS-logout palaa tänne, tarkistetaan keksi */
                String service = getServiceRedirectCookie(request);
                if (service != null) {
                    LOGGER.debug("Setting logout redirect url from service cookie: " + service);
                    WebUtils.putLogoutRedirectUrl(requestContext, service);
                    clearServiceRedirectCookie(response);
                }
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return null;
    }

    private String getServiceRedirectParameter(HttpServletRequest request) {
        return request.getParameter(casProperties.getLogout().getRedirectParameter());
    }

    /*
     * Parametrit, ml. SAMLin läpi kuljetettava RelayState, hukataan matkalla. Koska
     * myös sessio tuhotaan, hillotaan tieto paluuosoitteesta keksiin.
     */
    private void setServiceRedirectCookie(HttpServletResponse response, String value) {
        response.addCookie(serviceRedirectCookie(value));
    }

    private void clearServiceRedirectCookie(HttpServletResponse response) {
        response.addCookie(serviceRedirectCookie(null));
    }

    private Cookie serviceRedirectCookie(String value) {
        Cookie serviceRedirectCookie = new Cookie(SERVICE_COOKIE, value);
        serviceRedirectCookie.setHttpOnly(true);
        // tyhjä arvo -> keksi poistetaan
        serviceRedirectCookie.setMaxAge(value == null ? 0 : SERVICE_COOKIE_MAX_AGE);
        serviceRedirectCookie.setSecure(true);
        return serviceRedirectCookie;
    }

    private String getServiceRedirectCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies()).filter(
                (cookie) -> SERVICE_COOKIE.equals(cookie.getName())
        ).findFirst().map(Cookie::getValue).orElse(null);
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
