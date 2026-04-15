package fi.vm.sade;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.logout.LogoutRedirectionResponse;
import org.apereo.cas.logout.LogoutRedirectionStrategy;
import org.springframework.webflow.execution.RequestContextHolder;

import java.util.Optional;

/**
 * Redirects to the service that initiated SLO after the SAML logout round-trip completes.
 * <p>
 * When a service sends the user to {@code /cas-oppija/logout?service=<return-url>}, CAS does a
 * SAML logout round-trip with the IdP. The {@code return-url} travels through the round-trip in a
 * {@code TransientSessionTicket} and is placed in conversation scope by
 * {@link OtuvaDelegatedSaml2ClientLogoutAction} when the LogoutResponse arrives.
 * <p>
 * This strategy runs after {@link org.apereo.cas.logout.DefaultLogoutRedirectionStrategy}
 * (order {@value #ORDER}) and reinstates the service URL so it takes precedence over
 * {@code cas.view.default-redirect-url}.
 */
@Slf4j
public class OtuvaServiceInitiatedSloRedirectStrategy implements LogoutRedirectionStrategy {
    static final int ORDER = DEFAULT_ORDER + 500;
    static final String CONVERSATION_KEY = "otuvaLogoutRedirectTarget";

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public boolean supports(final HttpServletRequest request, final HttpServletResponse response) {
        return StringUtils.isNotBlank(getTargetFromConversationScope());
    }

    @Override
    public LogoutRedirectionResponse handle(final HttpServletRequest request, final HttpServletResponse response) {
        val target = getTargetFromConversationScope();
        LOGGER.debug("Redirecting to service-initiated SLO return URL: [{}]", target);
        return LogoutRedirectionResponse.builder()
            .logoutRedirectUrl(Optional.of(target))
            .build();
    }

    private String getTargetFromConversationScope() {
        val ctx = RequestContextHolder.getRequestContext();
        if (ctx == null) {
            return null;
        }
        return ctx.getConversationScope().get(CONVERSATION_KEY, String.class);
    }
}
