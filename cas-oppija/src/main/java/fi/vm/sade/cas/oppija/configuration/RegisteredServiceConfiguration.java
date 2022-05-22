package fi.vm.sade.cas.oppija.configuration;

import org.apereo.cas.services.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Map;

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
        return new CasOppijaAttributeReleasePolicy();
    }

    private static class CasOppijaAttributeReleasePolicy extends ReturnAllAttributeReleasePolicy {

        @Override
        protected Map<String, List<Object>> returnFinalAttributesCollection(Map<String, List<Object>> attributesToRelease, RegisteredService service) {
            // pac4j adds both saml name (e.g. urn:oid:2.5.4.3) and friendly name (e.g. cn) to principal attributes
            // and cas converts all attributes containing ":" into numbers which is not allowed in xml tags
            attributesToRelease.entrySet().removeIf(entry -> entry.getKey().contains(":"));
            return attributesToRelease;
        }
    }

    @Bean
    public List<RegisteredService> inMemoryRegisteredServices(Environment environment) {
        RegexRegisteredService regexRegisteredService = new RegexRegisteredService();
        regexRegisteredService.setId(1L);
        regexRegisteredService.setName("Services");
        // TODO tämä tuntuu hajottavan asiat vaikka logi huutaa puutteesta.
        //DefaultRegisteredServiceAccessStrategy strategy = new DefaultRegisteredServiceAccessStrategy();
        //DefaultRegisteredServiceDelegatedAuthenticationPolicy policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy(new LinkedHashSet<>(Arrays.asList("Suomi.fi", "Fakesuomi.fi")), true, false);
        //strategy.setDelegatedAuthenticationPolicy(policy);
        //regexRegisteredService.setAccessStrategy(strategy);
        regexRegisteredService.setServiceId(environment.getRequiredProperty("whitelist.regexp"));
        regexRegisteredService.setProxyPolicy(registeredServiceProxyPolicy());
        regexRegisteredService.setAttributeReleasePolicy(registeredServiceAttributeReleasePolicy());
        return singletonList(regexRegisteredService);
    }

}
