package fi.vm.sade.cas.oppija.configuration;

import org.apereo.cas.authentication.*;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.pac4j.core.profile.BasicUserProfile;
import org.pac4j.core.profile.UserProfile;
import org.springframework.context.annotation.Configuration;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.AUTHENTICATION_ATTRIBUTE_CLIENT_PRINCIPAL_ID;

@Configuration
public class AuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {

    @Override
    public void configureAuthenticationExecutionPlan(AuthenticationEventExecutionPlan plan) {
        plan.registerAuthenticationMetadataPopulator(new ClientAuthenticationMetaDataPopulator());
    }

    private static class ClientAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

        @Override
        public void populateAttributes(AuthenticationBuilder builder, AuthenticationTransaction transaction) {
            transaction.getPrimaryCredential()
                    .filter(ClientCredential.class::isInstance)
                    .map(ClientCredential.class::cast)
                    .ifPresent(credential -> populateAttributes(builder, credential));
        }

        private void populateAttributes(AuthenticationBuilder builder, ClientCredential credential) {
            // add attributes from saml profile to authentication attributes for saml logout support
            UserProfile profile = credential.getUserProfile();
            builder.addAttribute(AUTHENTICATION_ATTRIBUTE_CLIENT_PRINCIPAL_ID, profile.getId());
            if (profile instanceof BasicUserProfile) {
                ((BasicUserProfile) profile).getAuthenticationAttributes().forEach(builder::mergeAttribute);
            }
        }

        @Override
        public boolean supports(Credential credential) {
            return credential instanceof ClientCredential;
        }

    }

}
