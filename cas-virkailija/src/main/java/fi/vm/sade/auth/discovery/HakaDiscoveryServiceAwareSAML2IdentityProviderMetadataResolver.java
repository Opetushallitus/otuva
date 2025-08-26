package fi.vm.sade.auth.discovery;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.metadata.SAML2IdentityProviderMetadataResolver;
import org.springframework.webflow.execution.RequestContextHolder;

@Slf4j
public class HakaDiscoveryServiceAwareSAML2IdentityProviderMetadataResolver extends SAML2IdentityProviderMetadataResolver {
    public HakaDiscoveryServiceAwareSAML2IdentityProviderMetadataResolver(SAML2Configuration configuration) {
        super(configuration);
    }

    @Override
    public String getEntityId() {
        try {
            LOGGER.info("Reading Haka DS provided entity ID from request context");
            var requestContext = RequestContextHolder.getRequestContext();
            var flowScope = requestContext.getFlowScope();
            val entity = flowScope.get(
                    SamlDiscoveryWebflowConstants.FLOW_VAR_ID_DELEGATED_AUTHENTICATION_IDP,
                    SamlDiscoverySelectedIdP.class
            );
            if (entity != null) {
                LOGGER.info("Got Haka DS provided entity ID [{}] from request context ", entity.getEntityID());
                return entity.getEntityID();
            } else {
                LOGGER.warn("No entity ID found in request context; falling back to Valtori");
                return "http://fs.valtori.fi/adfs/services/trust";
            }
        } catch (Exception e) {
            LOGGER.error("Error getting Haka DS provided entity ID from request context; falling back to default", e);
            return super.getEntityId();
        }
    }
}
