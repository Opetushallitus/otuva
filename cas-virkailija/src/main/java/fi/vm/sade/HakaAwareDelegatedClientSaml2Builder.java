package fi.vm.sade;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientMetadataProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClient;
import org.apereo.cas.web.saml2.DelegatedClientSaml2Builder;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.metadata.*;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
public class HakaAwareDelegatedClientSaml2Builder extends DelegatedClientSaml2Builder {
    public HakaAwareDelegatedClientSaml2Builder(CasSSLContext casSslContext, ObjectProvider<SAMLMessageStoreFactory> samlMessageStoreFactory) {
        super(casSslContext, samlMessageStoreFactory);
    }

    @Override
    public List<ConfigurableDelegatedClient> build(CasConfigurationProperties casConfigurationProperties) {
        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            throw new RuntimeException("Could not initialize OpenSaml", e);
        }
        var hakaProperties = casConfigurationProperties
                .getAuthn()
                .getPac4j()
                .getSaml()
                .stream()
                .filter(p -> "haka".equals(p.getClientName()))
                .findFirst()
                .map(p -> createPropertiesForHakaClients(casConfigurationProperties, p));

        if (hakaProperties.isPresent()) {
            var saml = casConfigurationProperties.getAuthn().getPac4j().getSaml();
            saml.addAll(hakaProperties.get());
            casConfigurationProperties.getAuthn().getPac4j().setSaml(saml);
        }

        return super.build(casConfigurationProperties);
    }

    private List<Pac4jSamlClientProperties> createPropertiesForHakaClients(CasConfigurationProperties casConfigurationProperties,
                                                                           Pac4jSamlClientProperties hakaPac4jSamlClientProperties) {
        try {
            var load = new DefaultResourceLoader();
            var idpMetadataLocation = hakaPac4jSamlClientProperties.getMetadata().getIdentityProviderMetadataPath();
            var ipdMetadataAsString = load.getResource(idpMetadataLocation).getContentAsString(Charset.defaultCharset());
            var entities = extractIdpEntityDescriptors(ipdMetadataAsString);

            return entities
                    .stream()
                    .map(entityDescriptor -> {
                        String entityId = entityDescriptor.getEntityID();
                        String slug = "haka-" + entityId.toLowerCase(Locale.ROOT)
                                .replaceFirst("https://", "")
                                .replaceAll("[^a-z0-9]+", "-")
                                .replaceAll("^-+|-+$", "");

                        Path target = casConfigurationProperties
                                .getStandalone()
                                .getConfigurationDirectory()
                                .toPath()
                                .resolve(slug + ".xml");

                        try {
                            var xmlAsString = toXmlString(entityDescriptor);
                            Files.writeString(target, xmlAsString, StandardCharsets.UTF_8);
                        } catch (MarshallingException | TransformerException | IOException e) {
                            throw new RuntimeException("Failed to create haka clients", e);
                        }

                        var samlClientProperties = new Pac4jSamlClientProperties();
                        BeanUtils.copyProperties(hakaPac4jSamlClientProperties, samlClientProperties);
                        samlClientProperties.setClientName(slug);
                        var metadataProperties  = new Pac4jSamlClientMetadataProperties();
                        BeanUtils.copyProperties(samlClientProperties.getMetadata(), metadataProperties);
                        metadataProperties.setIdentityProviderMetadataPath("file://" + target);
                        samlClientProperties.setMetadata(metadataProperties);
                        return samlClientProperties;
                    }).toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<EntityDescriptor> extractIdpEntityDescriptors(String xml) throws Exception {
        Element root = parseDom(xml);
        XMLObject obj = unmarshall(root);

        final List<EntityDescriptor> result = new ArrayList<>();

        if (obj instanceof EntitiesDescriptor eds) {
            for (EntityDescriptor ed : eds.getEntityDescriptors()) {
                if (hasIdpSso(ed)) result.add(ed);
            }
            for (EntitiesDescriptor nested : eds.getEntitiesDescriptors()) {
                for (EntityDescriptor ed : nested.getEntityDescriptors()) {
                    if (hasIdpSso(ed)) result.add(ed);
                }
            }
        } else if (obj instanceof EntityDescriptor ed) {
            if (hasIdpSso(ed)) result.add(ed);
        } else {
            throw new IllegalArgumentException("Unsupported metadata root: " + obj.getElementQName());
        }

        return result;
    }

    private static boolean hasIdpSso(EntityDescriptor ed) {
        List<RoleDescriptor> roles = ed.getRoleDescriptors(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        return !roles.isEmpty();
    }

    private static Element parseDom(String xml) throws Exception {
        var dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        return doc.getDocumentElement();
    }

    private static XMLObject unmarshall(Element element) throws UnmarshallingException {
        UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
        if (unmarshaller == null) {
            throw new UnmarshallingException("No unmarshaller for element: " + element.getNodeName());
        }
        return unmarshaller.unmarshall(element);
    }

    public static String toXmlString(XMLObject object)
            throws MarshallingException, TransformerException {
        Element element = XMLObjectSupport.marshall(object);
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        try {
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        } catch (Exception ignored) {}
        var sw = new StringWriter();
        tf.transform(new DOMSource(element), new StreamResult(sw));
        return sw.toString();
    }
}
