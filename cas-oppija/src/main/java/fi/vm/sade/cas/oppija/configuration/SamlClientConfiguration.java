package fi.vm.sade.cas.oppija.configuration;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.properties.OphProperties;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static java.util.function.Function.identity;

@Configuration
public class SamlClientConfiguration {

    // override bean Pac4jAuthenticationEventExecutionPlanConfiguration#clientPrincipalFactory
    @Bean
    public PrincipalFactory clientPrincipalFactory(@Qualifier("oppijanumerorekisteriHttpClient") OphHttpClient httpClient, OphProperties properties) {
        return new HetuToOidPrincipalFactory(httpClient, properties);
    }

    private static class HetuToOidPrincipalFactory implements PrincipalFactory {

        private final OphHttpClient httpClient;
        private final OphProperties properties;
        private final PrincipalFactory principalFactory;

        public HetuToOidPrincipalFactory(OphHttpClient httpClient, OphProperties properties) {
            this.httpClient = httpClient;
            this.properties = properties;
            this.principalFactory = PrincipalFactoryUtils.newPrincipalFactory();
        }

        @Override
        public Principal createPrincipal(String hetu, Map<String, Object> attributes) {
            String url = properties.url("oppijanumerorekisteri-service.henkilo.byHetu.oid", hetu);
            OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
            String oid = httpClient.<String>execute(request)
                    .expectedStatus(200)
                    .mapWith(identity())
                    .orElseThrow(() -> new RuntimeException(String.format("Url %s returned 204 or 404", url)));
            return principalFactory.createPrincipal(oid, attributes);
        }

    }

}
