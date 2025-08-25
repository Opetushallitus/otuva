package fi.vm.sade.auth.discovery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.pac4j.core.client.Client;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class SamlDiscoveryClientCustomizer implements DelegatedClientFactoryCustomizer<Client> {
    @Override
    public void customize(Client client) {
        if (client instanceof SAML2Client saml2Client) {
            var config = saml2Client.getConfiguration();
            LOGGER.info("Customizing SAML2Client [{}] with HakaDiscoveryServiceAwareSAML2IdentityProviderMetadataResolver", client.getName());
            config.setIdentityProviderMetadataResolver(new HakaDiscoveryServiceAwareSAML2IdentityProviderMetadataResolver(config));
        }
    }
}
