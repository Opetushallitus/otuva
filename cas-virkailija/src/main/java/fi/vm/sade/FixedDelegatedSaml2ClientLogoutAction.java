package fi.vm.sade;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.support.pac4j.authentication.DelegatedAuthenticationClientLogoutRequest;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.pac4j.saml.credentials.SAML2Credentials;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Fixed to include a transaction which is required by the JPA ticketRegistry.getSessionsWithAttributes
 *
 * Remove if the original class org.apereo.cas.web.flow.actions.logout.DelegatedSaml2ClientLogoutAction ever gets fixed in CAS
 */
@Slf4j
@Transactional(transactionManager = "ticketTransactionManager")
@RequiredArgsConstructor
public class FixedDelegatedSaml2ClientLogoutAction extends BaseCasWebflowAction {
    private final TicketRegistry ticketRegistry;
    private final SingleLogoutRequestExecutor singleLogoutRequestExecutor;

    private void removeSsoSessionsForSessionIndexes(final HttpServletRequest request,
                                                    final HttpServletResponse response,
                                                    final LogoutRequest logoutRequest) {
        logoutRequest.getSessionIndexes().forEach(sessionIndex -> ticketRegistry
            .getSessionsWithAttributes(Map.of("sessionindex", List.of(Objects.requireNonNull(sessionIndex.getValue()))))
            .filter(ticket -> !ticket.isExpired())
            .map(TicketGrantingTicket.class::cast)
            .findFirst()
            .ifPresent(ticket -> singleLogoutRequestExecutor.execute(ticket.getId(), request, response)));
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val clientCredential = WebUtils.getCredential(requestContext, ClientCredential.class);

        if (clientCredential != null && clientCredential.getCredentials() instanceof final SAML2Credentials saml2Credentials) {
            val message = saml2Credentials.getContext().getMessageContext().getMessage();
            if (message instanceof final LogoutRequest logoutRequest && isDirectLogoutRequest(request)) {
                val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
                removeSsoSessionsForSessionIndexes(request, response, logoutRequest);
            }
            if (message instanceof final LogoutResponse logoutResponse) {
                val logoutRequestTicketId = TransientSessionTicketFactory.normalizeTicketId(logoutResponse.getInResponseTo());
                try {
                    val logoutRequestTicket = ticketRegistry.getTicket(logoutRequestTicketId, TransientSessionTicket.class);
                    if (logoutRequestTicket != null && !logoutRequestTicket.isExpired()) {
                        val delegatedAuthLogoutRequest = logoutRequestTicket.getProperty(DelegatedAuthenticationClientLogoutRequest.class.getName(),
                            DelegatedAuthenticationClientLogoutRequest.class);
                        DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequest(requestContext, delegatedAuthLogoutRequest);
                    }
                } catch (final InvalidTicketException e) {
                    LOGGER.info("Delegated authentication logout request ticket [{}] is not found", logoutRequestTicketId);
                    LOGGER.debug(e.getMessage(), e);
                } finally {
                    ticketRegistry.deleteTicket(logoutRequestTicketId);
                    DelegationWebflowUtils.putDelegatedAuthenticationLogoutRequestTicket(requestContext, null);
                }
            }
        }

        return success();
    }

    protected boolean isDirectLogoutRequest(final HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod())
            || request.getParameter(CasProtocolConstants.PARAMETER_LOGOUT_REQUEST) != null
            || request.getParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST) != null;
    }
}
