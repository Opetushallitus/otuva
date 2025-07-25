package fi.vm.sade.auth.discovery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.view.DynamicHtmlView;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

@Controller
@Slf4j
@RequiredArgsConstructor
@Getter
public class SamlDiscoveryReturnController {

    private final CasConfigurationProperties casProperties;
    private final DelegatedClientAuthenticationConfigurationContext configContext;

    @GetMapping("/discovery")
    public View redirectBackToWebflow(
            @RequestParam(required = true)
            final String flowId,
            final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        val ticket = configContext.getTicketRegistry().getTicket(flowId, TransientSessionTicket.class);
        String entityId = request.getParameter("entityID");
        LOGGER.info("Returning from discovery service with flowId [{}] and entityID [{}]", flowId, entityId);
        if(ticket != null) {
            LOGGER.info("Found ticket [{}] for flowId [{}]", ticket.getId(), flowId);
            configContext.getTicketRegistry().deleteTicket(ticket.getId());
            return new DynamicHtmlView(buildRedirectPostContent(ticket, entityId));
        }
        LOGGER.info("No ticket found for flowId [{}]. Redirecting to CAS login page", flowId);
        return new RedirectView(casProperties.getServer().getLoginUrl());
    }

    protected String buildRedirectPostContent(TransientSessionTicket ticket, String entityId) {
        val requestedUrl = casProperties.getServer().getLoginUrl();
        val buffer = new StringBuilder();
        buffer.append("<html>\n");
        buffer.append("<body>\n");
        buffer.append("<form action=\"").append(escapeHtml(requestedUrl)).append("\" name=\"f\" method=\"post\">\n");
        buffer.append("<input type='hidden' name=\"execution\" value=\"")
                .append(ticket.getProperty(SamlDiscoveryWebflowConstants.PROPERTY_ID_WEBFLOW_KEY, String.class))
                .append("\" />\n");
        buffer.append("<input type='hidden' name=\"")
                .append(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER)
                .append("\" value=\"")
                .append(ticket.getProperty(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, String.class))
                .append("\" />\n");
        buffer.append("<input type='hidden' name=\"entityID\" value=\"")
                .append(entityId)
                .append("\" />\n");
        buffer.append("<input type='hidden' name=\"_eventId\" value=\"success\" />\n");
        buffer.append("<input value='POST' type='submit' style='display: none;' />\n");
        buffer.append("</form>\n");
        buffer.append("<script type='text/javascript'>document.forms['f'].submit();</script>\n");
        buffer.append("</body>\n");
        buffer.append("</html>\n");
        return buffer.toString();
    }
}
