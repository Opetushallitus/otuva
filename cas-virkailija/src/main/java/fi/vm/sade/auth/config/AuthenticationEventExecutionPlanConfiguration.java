package fi.vm.sade.auth.config;

import fi.vm.sade.auth.cas.HttpAuthenticationHandler;
import fi.vm.sade.auth.clients.KayttooikeusClient;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.saml.action.SAMLAuthenticationHandler;
import lombok.RequiredArgsConstructor;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
    private final OphHttpClient httpClient;
    private final KayttooikeusClient kayttooikeusClient;

    @Override
    public void configureAuthenticationExecutionPlan(AuthenticationEventExecutionPlan plan) {
        plan.registerAuthenticationHandler(new HttpAuthenticationHandler(1, httpClient, kayttooikeusClient));
        plan.registerAuthenticationHandler(new SAMLAuthenticationHandler(2, kayttooikeusClient));
    }

}
