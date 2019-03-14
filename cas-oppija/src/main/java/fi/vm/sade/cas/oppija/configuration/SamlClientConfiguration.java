package fi.vm.sade.cas.oppija.configuration;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.properties.OphProperties;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import org.apereo.cas.support.pac4j.authentication.DelegatedClientFactory;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.pac4j.core.client.BaseClient;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.xml.namespace.QName;
import java.util.*;
import java.util.function.Supplier;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.DEFAULT_LANGUAGE;
import static fi.vm.sade.cas.oppija.CasOppijaConstants.SUPPORTED_LANGUAGES;
import static java.util.function.Function.identity;

@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlClientConfiguration {

    private final CasConfigurationProperties casProperties;

    public SamlClientConfiguration(CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

    // override bean Pac4jAuthenticationEventExecutionPlanConfiguration#clientPrincipalFactory
    //@Bean disabled for now, we might use hetu as principal after all
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

    @Bean
    public DelegatedClientFactory pac4jDelegatedClientFactory() {
        DelegatedClientFactory delegatedClientFactory = new DelegatedClientFactory(casProperties.getAuthn().getPac4j()) {
            @Override
            protected void configureClient(BaseClient client, Pac4jBaseClientProperties props) {
                super.configureClient(client, props);
                Map<String, String> customProperties = casProperties.getCustom().getProperties();
                if (client instanceof SAML2Client && Objects.equals(customProperties.get("suomiFiClientName"), client.getName())) {
                    SAML2Client saml2Client = (SAML2Client) client;
                    SAML2Configuration configuration = saml2Client.getConfiguration();
                    configuration.setAuthnRequestExtensions(createExtensions());
                }
            }
        };
        return delegatedClientFactory;
    }

    private Supplier<List<XSAny>> createExtensions() {
        return () -> {
            String language = Optional.ofNullable(LocaleContextHolder.getLocale())
                    .map(Locale::getLanguage)
                    .filter(SUPPORTED_LANGUAGES::contains)
                    .orElse(DEFAULT_LANGUAGE);
            return List.of(createLanguageExtension(language));
        };
    }

    /**
     * <vetuma xmlns="urn:vetuma:SAML:2.0:extensions">
     *     <LG>sv</LG>
     * </vetuma>
     */
    private XSAny createLanguageExtension(String languageCode) {
        XSAny lg = new XSAnyBuilder().buildObject(new QName("LG"));
        lg.setTextContent(languageCode);
        XSAny vetuma = new XSAnyBuilder().buildObject(new QName("urn:vetuma:SAML:2.0:extensions", "vetuma"));
        vetuma.getUnknownXMLObjects().add(lg);
        return vetuma;
    }

}
