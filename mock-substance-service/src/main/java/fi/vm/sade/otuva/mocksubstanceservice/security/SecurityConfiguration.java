package fi.vm.sade.otuva.mocksubstanceservice.security;

import lombok.RequiredArgsConstructor;
import org.apereo.cas.client.session.SingleSignOutFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Bean
    @Order(1)
    SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(
                        "/asd",
                        "/",
                        "/index")
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain casOppijaFilterChain(HttpSecurity http,
                 @Qualifier(CasOppijaConfiguration.CAS_OPPIJA_QUALIFIER) AuthenticationProvider casOppijaAuthProvider,
                 @Qualifier(CasOppijaConfiguration.CAS_OPPIJA_QUALIFIER) CasAuthenticationFilter casOppijaAuthFilter,
                 @Qualifier(CasOppijaConfiguration.CAS_OPPIJA_QUALIFIER) SingleSignOutFilter casOppijaSsoFilter,
                 @Qualifier(CasOppijaConfiguration.CAS_OPPIJA_QUALIFIER) SecurityContextRepository casOppijaCtxRepo,
                 @Qualifier(CasOppijaConfiguration.CAS_OPPIJA_QUALIFIER) CasAuthenticationEntryPoint casOppijaEntryPoint,
                 @Qualifier(CasOppijaConfiguration.CAS_OPPIJA_QUALIFIER) LogoutSuccessHandler casOppijaLogoutHandler) throws Exception {
        http.securityMatcher("/**")
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .authenticationProvider(casOppijaAuthProvider)
                .addFilterAt(casOppijaAuthFilter, CasAuthenticationFilter.class)
                .addFilterBefore(casOppijaSsoFilter, CasAuthenticationFilter.class)
                .securityContext(
                        context -> context.requireExplicitSave(true).securityContextRepository(casOppijaCtxRepo))
                .exceptionHandling(
                        handler -> handler.authenticationEntryPoint(casOppijaEntryPoint))
                .logout(
                        logout -> logout.logoutUrl("/logout").logoutSuccessHandler(casOppijaLogoutHandler));

        return http.build();
    }

    @Bean
    JdbcSessionMappingStorage jdbcSessionMappingStorage(
            JdbcTemplate jdbcTemplate, SessionRepository<? extends Session> sessionRepository) {
        return new JdbcSessionMappingStorage(jdbcTemplate, sessionRepository);
    }

    @Bean
    SessionRepository<? extends Session> sessionRepository(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        SessionRepository<? extends Session> sessionRepository = new JdbcIndexedSessionRepository(jdbcTemplate, transactionTemplate);
        return sessionRepository;
    }
}
