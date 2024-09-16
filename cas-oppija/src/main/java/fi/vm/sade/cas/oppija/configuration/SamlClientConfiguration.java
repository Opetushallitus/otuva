package fi.vm.sade.cas.oppija.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import fi.vm.sade.cas.oppija.exception.SystemException;
import fi.vm.sade.cas.oppija.service.PersonService;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.provision.DelegatedClientUserProfileProvisioner;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jDelegatedAuthenticationCoreProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.pac4j.authentication.clients.DefaultDelegatedClientFactory;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactory;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.apereo.cas.support.pac4j.authentication.handler.support.DelegatedClientAuthenticationHandler;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.logout.handler.LogoutHandler;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.xml.namespace.QName;
import java.util.*;
import java.util.function.Supplier;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.*;
import static fi.vm.sade.cas.oppija.CasOppijaUtils.resolveAttribute;
import static org.pac4j.core.util.Pac4jConstants.ELEMENT_SEPARATOR;

@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlClientConfiguration.class);

    private final CasConfigurationProperties casProperties;

    public SamlClientConfiguration(CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

    // override bean Pac4jAuthenticationEventExecutionPlanConfiguration#clientPrincipalFactory
    @Bean
    public PrincipalFactory clientPrincipalFactory(PersonService personService) {
        return new OidAttributePrincipalFactory(personService);
    }

    private static class OidAttributePrincipalFactory implements PrincipalFactory {

        private final PersonService personService;
        private final PrincipalFactory principalFactory;

        public OidAttributePrincipalFactory(PersonService personService) {
            this.personService = personService;
            this.principalFactory = PrincipalFactoryUtils.newPrincipalFactory();
        }

        @Override
        public Principal createPrincipal(String id, Map<String, List<Object>> attributes) {
            try {
                Optional<String> oidByHetu = resolveNationalIdentificationNumber(attributes)
                    .flatMap(this::findOidByNationalIdentificationNumber);
                if (oidByHetu.isPresent()) {
                    attributes.put(ATTRIBUTE_NAME_PERSON_OID, List.of(oidByHetu.get()));
                } else {
                    resolveEidasId(attributes)
                        .flatMap(this::findOidByEidasId)
                        .ifPresent((oid) -> attributes.put(ATTRIBUTE_NAME_PERSON_OID, List.of(oid)));
                }
            } catch (Exception e) {
                LOGGER.error("Unable to get oid", e);
            }

            return principalFactory.createPrincipal(id, attributes);
        }

        private Optional<String> resolveNationalIdentificationNumber(Map<String, List<Object>> attributes) {
            return resolveAttribute(attributes, ATTRIBUTE_NAME_NATIONAL_IDENTIFICATION_NUMBER, String.class);
        }

        private Optional<String> resolveEidasId(Map<String, List<Object>> attributes) {
            return resolveAttribute(attributes, ATTRIBUTE_NAME_EIDAS_ID, String.class);
        }

        private Optional<String> findOidByNationalIdentificationNumber(String nationalIdentificationNumber) {
            return personService.findOidByNationalIdentificationNumber(nationalIdentificationNumber);
        }

        private Optional<String> findOidByEidasId(String eidasId) throws SystemException {
            return personService.findOidByEidasId(eidasId);
        }
    }

    // override bean Pac4jAuthenticationEventExecutionPlanConfiguration#clientAuthenticationHandler
    @Bean
    public AuthenticationHandler clientAuthenticationHandler(ObjectProvider<ServicesManager> servicesManager, PersonService personService, Clients builtClients, @Qualifier(DelegatedClientUserProfileProvisioner.BEAN_NAME) final DelegatedClientUserProfileProvisioner clientUserProfileProvisioner, @Qualifier("delegatedClientDistributedSessionStore") final SessionStore delegatedClientDistributedSessionStore) {
        var pac4j = casProperties.getAuthn().getPac4j().getCore();
        var h = new DelegatedClientAuthenticationHandler(pac4j.getName(), pac4j.getOrder(), servicesManager.getIfAvailable(), clientPrincipalFactory(personService), builtClients, clientUserProfileProvisioner, delegatedClientDistributedSessionStore) {
            @Override
            protected String determinePrincipalIdFrom(UserProfile profile, BaseClient client) {
                String id = super.determinePrincipalIdFrom(profile, client);
                return profile.getClientName() + ELEMENT_SEPARATOR + id;
            }
        };
        h.setTypedIdUsed(pac4j.isTypedIdUsed());
        h.setPrincipalAttributeId(pac4j.getPrincipalAttributeId());
        return h;
    }

    @Bean
    public DelegatedClientFactory pac4jDelegatedClientFactory(Collection<DelegatedClientFactoryCustomizer> customizers, CasSSLContext casSSLContext, ApplicationContext applicationContext, ObjectProvider<SAMLMessageStoreFactory> samlMessageStoreFactory) {
        Pac4jDelegatedAuthenticationCoreProperties core = casProperties.getAuthn().getPac4j().getCore();
        Cache<String, Collection<IndirectClient>> clientsCache = Caffeine.newBuilder()
                .maximumSize(core.getCacheSize())
                .expireAfterAccess(Beans.newDuration(core.getCacheDuration()))
                .build();
        return new DefaultDelegatedClientFactory(casProperties, customizers, casSSLContext, samlMessageStoreFactory, clientsCache) {

            @Override
            protected Collection<IndirectClient> loadClients() {// (IndirectClient client, Pac4jBaseClientProperties props) {
                Collection<IndirectClient> clients = buildSaml2IdentityProviders(casProperties);
                Map<String, String> customProperties = casProperties.getCustom().getProperties();
                for (IndirectClient client : clients) {
                    if (client instanceof SAML2Client && (Objects.equals(customProperties.get("suomiFiClientName"), client.getName()) || Objects.equals(customProperties.get("fakeSuomiFiClientName"), client.getName()))) {
                        SAML2Client saml2Client = (SAML2Client) client;
                        SAML2Configuration configuration = saml2Client.getConfiguration();
                        configuration.setSpLogoutRequestBindingType(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
                        configuration.setSpLogoutResponseBindingType(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
                        configuration.setSpLogoutRequestSigned(true);
                        configuration.setLogoutHandler(new LogoutHandler() {});
                        configuration.setAuthnRequestExtensions(createExtensions());
                        client.init();
                    }
                }
               return clients;
            }
        };
    }

    private Supplier<List<XSAny>> createExtensions() {
        return () -> {
            String language = Optional.of(LocaleContextHolder.getLocale()).map(Locale::getLanguage).filter(SUPPORTED_LANGUAGES::contains).orElse(DEFAULT_LANGUAGE);
            return List.of(createLanguageExtension(language));
        };
    }


    /**
     * <vetuma xmlns="urn:vetuma:SAML:2.0:extensions">
     * <LG>sv</LG>
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

