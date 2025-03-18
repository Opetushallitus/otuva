package fi.vm.sade.auth.config;

import fi.vm.sade.auth.cas.DelegatedAuthenticationHandler;
import fi.vm.sade.auth.cas.HttpAuthenticationHandler;
import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.saml.action.SAMLAuthenticationHandler;
import lombok.val;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.provision.DelegatedClientUserProfileProvisioner;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.ServicesManager;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
    private final ServicesManager servicesManager;
    private final OphHttpClient httpClient;
    private final KayttooikeusRestClient kayttooikeusRestClient;
    final ConfigurableApplicationContext applicationContext;
    final CasConfigurationProperties casProperties;
    final PrincipalFactory clientPrincipalFactory;
    final DelegatedIdentityProviders identityProviders;
    final DelegatedClientUserProfileProvisioner clientUserProfileProvisioner;
    final SessionStore delegatedClientDistributedSessionStore;

    public AuthenticationEventExecutionPlanConfiguration(
            ServicesManager servicesManager,
            OphHttpClient httpClient,
            KayttooikeusRestClient kayttooikeusRestClient,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("clientPrincipalFactory") final PrincipalFactory clientPrincipalFactory,
            @Qualifier("delegatedIdentityProviders") final DelegatedIdentityProviders identityProviders,
            @Qualifier("clientUserProfileProvisioner") final DelegatedClientUserProfileProvisioner clientUserProfileProvisioner,
            @Qualifier("delegatedClientDistributedSessionStore") final SessionStore delegatedClientDistributedSessionStore) {
        this.servicesManager = servicesManager;
        this.httpClient = httpClient;
        this.kayttooikeusRestClient = kayttooikeusRestClient;
        this.applicationContext = applicationContext;
        this.casProperties = casProperties;
        this.clientPrincipalFactory = clientPrincipalFactory;
        this.identityProviders = identityProviders;
        this.clientUserProfileProvisioner = clientUserProfileProvisioner;
        this.delegatedClientDistributedSessionStore = delegatedClientDistributedSessionStore;
    }

    @Override
    public void configureAuthenticationExecutionPlan(AuthenticationEventExecutionPlan plan) {
        plan.registerAuthenticationHandler(new HttpAuthenticationHandler(servicesManager, 1, httpClient));
        plan.registerAuthenticationHandler(new SAMLAuthenticationHandler(2, kayttooikeusRestClient));

        val pac4j = casProperties.getAuthn().getPac4j().getCore();
        plan.registerAuthenticationHandler(new DelegatedAuthenticationHandler(pac4j,
                servicesManager, clientPrincipalFactory, identityProviders, clientUserProfileProvisioner,
                delegatedClientDistributedSessionStore, applicationContext, kayttooikeusRestClient));
    }

}
