package fi.vm.sade.kayttooikeus.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@Order(2)
@ConditionalOnProperty(value = "userdetails.use-basic-auth", havingValue = "true", matchIfMissing = false)
public class UserDetailsSecurityConfig extends WebSecurityConfigurerAdapter {
    public static final String ROLE = "APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_READ";

    @Value("${userdetails.username}")
    private String username;

    @Value("${userdetails.password}")
    private String password;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .requestMatchers()
                .antMatchers("/userDetails/**")
                .and()
                .csrf().disable()
                .headers().disable()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.NEVER);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(username)
                .password("{noop}" + password)
                .roles(ROLE);
    }
}
