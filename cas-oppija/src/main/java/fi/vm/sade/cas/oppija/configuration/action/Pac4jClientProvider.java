package fi.vm.sade.cas.oppija.configuration.action;

import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.saml.profile.SAML2Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pac4jClientProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pac4jClientProvider.class);

    private final Clients clients;

    public Pac4jClientProvider(Clients clients) {
        this.clients = clients;
    }

    public Client getClient(SAML2Profile profile) {
        Client<?, ?> client = null;
        try {
            var currentClientName = profile == null ? null : profile.getClientName();
            client = currentClientName == null ? null : clients.findClient(currentClientName);
        } catch (final TechnicalException e) {
            LOGGER.debug("No SAML2 client found: " + e.getMessage(), e);
        }
        return client;
    }
}
