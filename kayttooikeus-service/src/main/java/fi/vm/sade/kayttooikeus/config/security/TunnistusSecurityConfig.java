package fi.vm.sade.kayttooikeus.config.security;

import fi.vm.sade.kayttooikeus.config.security.casoppija.SuomiFiAuthenticationDetailsSource;
import fi.vm.sade.kayttooikeus.config.security.casoppija.SuomiFiAuthenticationProcessingFilter;

import org.apereo.cas.client.validation.Cas30ProxyTicketValidator;
import org.apereo.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesUserDetailsService;

@Configuration
public class TunnistusSecurityConfig {
    public static final String OPPIJA_TICKET_VALIDATOR_QUALIFIER = "oppijaTicketValidator";
    public static final String OPPIJA_CAS_TUNNISTUS_PATH = "/cas/tunnistus";

    @Value("${cas.oppija.url}")
    private String casOppijaUrl;

    @Bean
    SuomiFiAuthenticationDetailsSource suomiFiAuthenticationDetailsSource() {
        return new SuomiFiAuthenticationDetailsSource();
    }

    @Bean(name = OPPIJA_TICKET_VALIDATOR_QUALIFIER)
    TicketValidator oppijaTicketValidator() {
        return new Cas30ProxyTicketValidator(casOppijaUrl);
    }

    @Bean
    SuomiFiAuthenticationProcessingFilter suomiFiAuthenticationProcessingFilter(HttpSecurity http, PreAuthenticatedAuthenticationProvider authenticationProvider,
            @Qualifier(OPPIJA_TICKET_VALIDATOR_QUALIFIER) TicketValidator ticketValidator) throws Exception {
        SuomiFiAuthenticationProcessingFilter filter =
                new SuomiFiAuthenticationProcessingFilter(ticketValidator);
        filter.setAuthenticationManager(new ProviderManager(authenticationProvider));
        filter.setAuthenticationDetailsSource(suomiFiAuthenticationDetailsSource());
        return filter;
    }

    @Bean
    PreAuthenticatedAuthenticationProvider authenticationProvider() {
        PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
        provider.setPreAuthenticatedUserDetailsService(new PreAuthenticatedGrantedAuthoritiesUserDetailsService());
        return provider;
    }

    @Bean
    @Order(2)
    SecurityFilterChain suomiFiFilterChain(HttpSecurity http, SuomiFiAuthenticationProcessingFilter filter,
            PreAuthenticatedAuthenticationProvider authenticationProvider) throws Exception {
        http
            .headers(headers -> headers.disable())
            .csrf(csrf -> csrf.disable())
            .securityMatcher(OPPIJA_CAS_TUNNISTUS_PATH)
            .authorizeHttpRequests(authz -> authz.requestMatchers(OPPIJA_CAS_TUNNISTUS_PATH).authenticated())
            .authenticationProvider(authenticationProvider)
            .addFilter(filter);
        return http.build();
    }
}
