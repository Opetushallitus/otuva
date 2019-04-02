package fi.vm.sade.cas.oppija.configuration.action;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.saml.profile.SAML2Profile;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.AUTHENTICATION_ATTRIBUTE_CLIENT_PRINCIPAL_ID;
import static fi.vm.sade.cas.oppija.CasOppijaConstants.REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT;
import static java.util.stream.Collectors.toMap;
import static org.apereo.cas.authentication.principal.ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME;
import static org.apereo.cas.util.CollectionUtils.firstElement;

/**
 * Adds SAML profile to request scope to create identity provider logout after service provider logout.
 *
 * @see SamlLogoutExecuteAction
 */
public class SamlLogoutPrepareAction extends AbstractAction {

    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private final TicketRegistrySupport ticketRegistrySupport;

    public SamlLogoutPrepareAction(CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                   TicketRegistrySupport ticketRegistrySupport) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.ticketRegistrySupport = ticketRegistrySupport;
    }

    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        Authentication authentication = getAuthentication(context);
        if (authentication != null) {
            SAML2Profile profile = new SAML2Profile();
            firstElement(authentication.getAttributes().get(AUTHENTICATION_ATTRIBUTE_CLIENT_NAME))
                    .filter(String.class::isInstance).map(String.class::cast).ifPresent(profile::setClientName);
            firstElement(authentication.getAttributes().get(AUTHENTICATION_ATTRIBUTE_CLIENT_PRINCIPAL_ID))
                    .filter(String.class::isInstance).map(String.class::cast).ifPresent(profile::setId);

            Principal principal = authentication.getPrincipal();
            profile.addAttributes(transformAttributes(principal.getAttributes()));
            profile.addAuthenticationAttributes(transformAttributes(authentication.getAttributes()));

            context.getRequestScope().put(REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT, profile);
        }
        return success();
    }

    private Authentication getAuthentication(RequestContext context) {
        String ticketGrantingTicketId = getTicketGrantingTicketId(context);
        if (ticketGrantingTicketId == null) {
            return null;
        }
        return ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicketId);
    }

    private String getTicketGrantingTicketId(RequestContext context) {
        HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        return ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
    }

    protected static Map<String, Object> transformAttributes(Map<String, Object> attributes) {
        // cas stores attribute value as list but pac4j expects them mostly be single item
        return attributes.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> firstElement(e.getValue()).orElse(null)));
    }

}
