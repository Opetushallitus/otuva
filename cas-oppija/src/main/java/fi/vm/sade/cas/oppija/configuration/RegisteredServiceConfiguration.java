package fi.vm.sade.cas.oppija.configuration;

import org.apereo.cas.services.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static org.pac4j.saml.credentials.authenticator.SAML2Authenticator.SESSION_INDEX;

@Configuration
public class RegisteredServiceConfiguration {

    private static final Pattern SESSION_INDEX_PATTERN = Pattern.compile(Pattern.quote(SESSION_INDEX), Pattern.CASE_INSENSITIVE);
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
        return new CasOppijaAttributeReleasePolicy();
    }

    private static class CasOppijaAttributeReleasePolicy extends ReturnAllAttributeReleasePolicy {

        @Override
        protected Map<String, Object> returnFinalAttributesCollection(Map<String, Object> attributesToRelease, RegisteredService service) {
            // pac4j adds session index to principal attributes (should be only in auth attrs), fixed in pac4j 4.0
            attributesToRelease.entrySet().removeIf(entry -> SESSION_INDEX_PATTERN.matcher(entry.getKey()).find());
            // pac4j adds both saml name (e.g. urn:oid:2.5.4.3) and friendly name (e.g. cn) to principal attributes
            // and cas converts all attributes containing ":" into numbers which is not allowed in xml tags
            attributesToRelease.entrySet().removeIf(entry -> entry.getKey().contains(":"));
            return attributesToRelease;
        }

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
