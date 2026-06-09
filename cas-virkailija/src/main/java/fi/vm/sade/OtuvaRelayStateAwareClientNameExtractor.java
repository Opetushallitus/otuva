package fi.vm.sade;

import org.apereo.cas.pac4j.client.DelegatedClientNameExtractor;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Resolves the per-IdP pac4j client name from the SAML RelayState ticket.
 * Works around CAS 8's identity-provider-metadata-aggregate=true mode: the
 * configured client_name ("haka") is replaced at runtime by per-IdP names
 * ("haka-&lt;sha256(entityID)&gt;"), but the shared SP entity ID means the
 * SAML callback URL still carries only the base name and the default
 * extractor cannot identify which per-IdP client should consume the
 * response.
 */
@Slf4j
@RequiredArgsConstructor
public class OtuvaRelayStateAwareClientNameExtractor implements DelegatedClientNameExtractor {

    private static final String PAC4J_CLIENT_PROPERTY = "org.pac4j.core.client.Client";

    private final TicketRegistry ticketRegistry;

    private final DelegatedClientNameExtractor fallback = DelegatedClientNameExtractor.fromHttpRequest();

    @Override
    public Optional<String> extract(final HttpServletRequest request) {
        val relayState = request.getParameter("RelayState");
        if (StringUtils.isNotBlank(relayState) && relayState.startsWith(TransientSessionTicket.PREFIX + '-')) {
            val fromTicket = lookupClientNameFromTicket(relayState);
            if (fromTicket.isPresent()) {
                LOGGER.debug("Resolved delegated client name [{}] from RelayState ticket [{}]",
                    fromTicket.get(), relayState);
                return fromTicket;
            }
        }
        return fallback.extract(request);
    }

    private Optional<String> lookupClientNameFromTicket(final String ticketId) {
        try {
            val ticket = ticketRegistry.getTicket(ticketId, TransientSessionTicket.class);
            if (ticket != null) {
                val name = ticket.getProperty(PAC4J_CLIENT_PROPERTY, String.class);
                if (StringUtils.isNotBlank(name)) {
                    return Optional.of(name);
                }
            }
        } catch (final Throwable e) {
            LOGGER.debug("Could not resolve delegated client name from RelayState ticket [{}]: {}",
                ticketId, e.getMessage());
        }
        return Optional.empty();
    }
}
