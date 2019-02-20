package fi.vm.sade.cas.oppija.configuration;

import org.apereo.cas.services.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

import static java.util.Collections.singletonList;

@Configuration
public class RegisteredServiceConfiguration {

    private final Environment environment;

    public RegisteredServiceConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public RegisteredServiceProxyPolicy registeredServiceProxyPolicy() {
        return new RegexMatchingRegisteredServiceProxyPolicy(environment.getRequiredProperty("whitelist.regexp"));
    }

    @Bean
    public RegisteredServiceAttributeReleasePolicy registeredServiceAttributeReleasePolicy() {
        return new ReturnAllAttributeReleasePolicy();
    }

    @Bean
    public List<? extends RegisteredService> inMemoryRegisteredServices(Environment environment) {
        RegexRegisteredService regexRegisteredService = new RegexRegisteredService();
        regexRegisteredService.setId(1L);
        regexRegisteredService.setName("Services");
        regexRegisteredService.setServiceId(environment.getRequiredProperty("whitelist.regexp"));
        regexRegisteredService.setProxyPolicy(registeredServiceProxyPolicy());
        regexRegisteredService.setAttributeReleasePolicy(registeredServiceAttributeReleasePolicy());
        return singletonList(regexRegisteredService);
    }

}
