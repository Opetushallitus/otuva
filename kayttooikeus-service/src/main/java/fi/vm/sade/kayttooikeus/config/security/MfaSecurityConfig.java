package fi.vm.sade.kayttooikeus.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import fi.vm.sade.kayttooikeus.config.properties.CasProperties;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@Configuration
public class MfaSecurityConfig {
  public static final String ROLE = "APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_READ";

  private CasProperties casProperties;

  public MfaSecurityConfig(CasProperties casProperties) {
    this.casProperties = casProperties;
  }

  @Bean
  @Order(1)
  SecurityFilterChain mfaFilterChain(HttpSecurity http) throws Exception {
    http
        .headers(headers -> headers.disable())
        .csrf(csrf -> csrf.disable())
        .securityMatcher("/mfa/**")
        .authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
        .httpBasic(withDefaults())
        .authenticationManager(mfaBasicAuthManager())
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.NEVER)
        );
    return http.build();
  }

  AuthenticationManager mfaBasicAuthManager() {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(mfaBasicAuthUsers());
    return new ProviderManager(authenticationProvider);
  }

  UserDetailsService mfaBasicAuthUsers() {
    UserDetails specialUser = User.withUsername(casProperties.getMfa().getUsername())
        .password("{noop}" + casProperties.getMfa().getPassword())
        .roles(ROLE)
        .build();

    return new InMemoryUserDetailsManager(specialUser);
  }
}
