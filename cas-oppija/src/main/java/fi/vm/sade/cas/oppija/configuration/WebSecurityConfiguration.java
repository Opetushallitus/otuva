package fi.vm.sade.cas.oppija.configuration;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.security.CasWebSecurityConfigurerAdapter;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * This class should include only fixes to default cas security configuration.
 */
@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WebSecurityConfiguration {

    private final CasConfigurationProperties casProperties;
    private final SecurityProperties securityProperties;
    private final WebEndpointProperties webEndpointProperties;
    private final PathMappedEndpoints pathMappedEndpoints;

    public WebSecurityConfiguration(CasConfigurationProperties casProperties, SecurityProperties securityProperties,
                                    WebEndpointProperties webEndpointProperties,
                                    PathMappedEndpoints pathMappedEndpoints) {
        this.casProperties = casProperties;
        this.securityProperties = securityProperties;
        this.webEndpointProperties = webEndpointProperties;
        this.pathMappedEndpoints = pathMappedEndpoints;
    }

    // it seems that we get header x-forwarded-proto=http from internal proxy which cas redirects to https creating
    // endless loop. this bean removes x-forwarded-proto related logic from casWebSecurityConfigurerAdapter.
    @Bean
    public CasWebSecurityConfigurerAdapter casWebSecurityConfigurerAdapter() {
        return new CustomCasWebSecurityConfigurerAdapter(casProperties, securityProperties, webEndpointProperties, pathMappedEndpoints);
    }

    public class CustomCasWebSecurityConfigurerAdapter extends CasWebSecurityConfigurerAdapter {

        private final CasConfigurationProperties casProperties;

        public CustomCasWebSecurityConfigurerAdapter(CasConfigurationProperties casProperties,
                                                     SecurityProperties securityProperties,
                                                     WebEndpointProperties webEndpointProperties,
                                                     PathMappedEndpoints pathMappedEndpoints) {
            super(casProperties, securityProperties, webEndpointProperties, pathMappedEndpoints);
            this.casProperties = casProperties;
        }

        @Override
        protected void configure(final HttpSecurity http) throws Exception {

            http.csrf().disable()
                    .headers().disable()
                    .logout()
                    .disable();

            var requests = http.authorizeRequests();
            var endpoints = casProperties.getMonitor().getEndpoints().getEndpoint();
            endpoints.forEach(Unchecked.biConsumer((k, v) -> {
                var endpoint = EndpointRequest.to(k);
                v.getAccess().forEach(Unchecked.consumer(access -> configureEndpointAccess(http, requests, access, v, endpoint)));
            }));
            configureEndpointAccessToDenyUndefined(http, requests);
            configureEndpointAccessForStaticResources(requests);
        }

    }

}
