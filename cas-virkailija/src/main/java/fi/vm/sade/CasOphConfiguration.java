package fi.vm.sade;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;

import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import fi.vm.sade.auth.cas.DelegatedAuthenticationProcessor;
import fi.vm.sade.auth.clients.KayttooikeusRestClient;

@Configuration
@ComponentScan
@RequiredArgsConstructor
public class CasOphConfiguration {
    final PrincipalFactory principalFactory;
    final KayttooikeusRestClient kayttooikeusRestClient;

    @Bean
    public ObservationRegistry observationRegistry() {
        // Disable all observations
        return ObservationRegistry.NOOP;
    }

    @Bean
    public DelegatedAuthenticationPreProcessor myProcessor() {
        return new DelegatedAuthenticationProcessor(principalFactory, kayttooikeusRestClient);
    }
}
