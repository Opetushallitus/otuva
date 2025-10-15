package fi.vm.sade.auth.config;

import fi.vm.sade.auth.cas.HttpAuthenticationHandler;
import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.saml.action.SAMLAuthenticationHandler;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
    private final OphHttpClient httpClient;
    private final KayttooikeusRestClient kayttooikeusRestClient;

    public AuthenticationEventExecutionPlanConfiguration(
            OphHttpClient httpClient,
            KayttooikeusRestClient kayttooikeusRestClient) {
        this.httpClient = httpClient;
        this.kayttooikeusRestClient = kayttooikeusRestClient;
    }

    @Override
    public void configureAuthenticationExecutionPlan(AuthenticationEventExecutionPlan plan) {
        plan.registerAuthenticationHandler(new HttpAuthenticationHandler(1, httpClient));
        plan.registerAuthenticationHandler(new SAMLAuthenticationHandler(2, kayttooikeusRestClient));
    }

}
