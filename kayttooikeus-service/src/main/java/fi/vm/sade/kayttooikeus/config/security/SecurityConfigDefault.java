package fi.vm.sade.kayttooikeus.config.security;

import fi.vm.sade.java_utils.security.OpintopolkuCasAuthenticationFilter;
import fi.vm.sade.kayttooikeus.config.properties.CasProperties;
import fi.vm.sade.properties.OphProperties;

import org.apereo.cas.client.session.SessionMappingStorage;
import org.apereo.cas.client.session.SingleSignOutFilter;
import org.apereo.cas.client.validation.Cas30ProxyTicketValidator;
import org.apereo.cas.client.validation.TicketValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

@Configuration
@EnableMethodSecurity(jsr250Enabled = false, prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
public class SecurityConfigDefault {
    private CasProperties casProperties;
    private OphProperties ophProperties;
    private SessionMappingStorage sessionMappingStorage;

    public static final String SPRING_CAS_SECURITY_CHECK_PATH = "/j_spring_cas_security_check";

    public SecurityConfigDefault(CasProperties casProperties, OphProperties ophProperties,
                                 SessionMappingStorage sessionMappingStorage) {
        this.casProperties = casProperties;
        this.ophProperties = ophProperties;
        this.sessionMappingStorage = sessionMappingStorage;
    }

    @Bean
    ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(casProperties.getService() + SPRING_CAS_SECURITY_CHECK_PATH);
        serviceProperties.setSendRenew(casProperties.getSendRenew());
        serviceProperties.setAuthenticateAllArtifacts(true);
        return serviceProperties;
    }

    //
    // CAS authentication provider (authentication manager)
    //

    @Bean
    CasAuthenticationProvider casAuthenticationProvider() {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setAuthenticationUserDetailsService(new OpintopolkuUserDetailsService());
        casAuthenticationProvider.setServiceProperties(serviceProperties());
        casAuthenticationProvider.setTicketValidator(ticketValidator());
        casAuthenticationProvider.setKey(casProperties.getKey());
        return casAuthenticationProvider;
    }

    @Bean
    TicketValidator ticketValidator() {
        var validator = new Cas30ProxyTicketValidator(ophProperties.url("cas.url"));
        validator.setAcceptAnyProxy(true);
        return validator;
    }

    @Bean
    HttpSessionSecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    //
    // CAS filter
    //

    @Bean
    CasAuthenticationFilter casAuthenticationFilter(
            ServiceProperties serviceProperties,
            SecurityContextRepository securityContextRepository) throws Exception {
        CasAuthenticationFilter casAuthenticationFilter = new OpintopolkuCasAuthenticationFilter(serviceProperties);
        casAuthenticationFilter.setAuthenticationManager(new ProviderManager(casAuthenticationProvider()));
        casAuthenticationFilter.setServiceProperties(serviceProperties);
        casAuthenticationFilter.setFilterProcessesUrl(SPRING_CAS_SECURITY_CHECK_PATH);
        casAuthenticationFilter.setSecurityContextRepository(securityContextRepository);
        return casAuthenticationFilter;
    }

    //
    // CAS single logout filter
    // requestSingleLogoutFilter is not configured because our users always sign out through CAS logout (using virkailija-raamit
    // logout button) when CAS calls this filter if user has ticket to this service.
    //
    @Bean
    SingleSignOutFilter singleSignOutFilter() {
        SingleSignOutFilter.setSessionMappingStorage(sessionMappingStorage);
        SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
        singleSignOutFilter.setIgnoreInitConfiguration(true);
        return singleSignOutFilter;
    }

    //
    // CAS entry point
    //

    @Bean
    CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
        CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setLoginUrl(ophProperties.url("cas.login"));
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
        return casAuthenticationEntryPoint;
    }

    @Bean
    @Order(3)
    SecurityFilterChain restApiFilterChain(HttpSecurity http, CasAuthenticationFilter casAuthenticationFilter,
            AuthenticationEntryPoint authenticationEntryPoint, SecurityContextRepository securityContextRepository) throws Exception {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);
        http
            .headers(headers -> headers.disable())
            .csrf(csrf -> csrf.disable())
            .securityMatcher("/**")
            .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/buildversion.txt").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/kutsu/token/*").permitAll()
                    .requestMatchers("/cas/uudelleenrekisterointi").permitAll()
                    .requestMatchers("/cas/henkilo/loginToken/*").permitAll()
                    .requestMatchers("/cas/emailverification/*").permitAll()
                    .requestMatchers("/cas/emailverification/loginTokenValidation/*").permitAll()
                    .requestMatchers("/cas/emailverification/redirectByLoginToken/*").permitAll()
                    .requestMatchers("/cas/salasananvaihto").permitAll()
                    .requestMatchers("/cas/loginparams").permitAll()
                    .requestMatchers("/cas/tunnistus").permitAll()
                    .requestMatchers("/userDetails", "/userDetails/*").permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/", "/swagger-ui/**", "/swagger-resources/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated())
            .addFilterAt(casAuthenticationFilter, CasAuthenticationFilter.class)
            .addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class)
            .securityContext(securityContext -> securityContext
                .requireExplicitSave(true)
                .securityContextRepository(securityContextRepository))
            .requestCache(cache -> cache.requestCache(requestCache))
            .exceptionHandling(handling -> handling.authenticationEntryPoint(authenticationEntryPoint));
        return http.build();
    }
}
