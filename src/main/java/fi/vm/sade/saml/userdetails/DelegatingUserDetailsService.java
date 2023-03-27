package fi.vm.sade.saml.userdetails;

import java.util.List;

import fi.vm.sade.saml.exception.RequiredSamlAttributeNotProvidedException;
import fi.vm.sade.saml.exception.SAMLCredentialsParseException;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

import java.util.ArrayList;

public class DelegatingUserDetailsService implements SAMLUserDetailsService {
    private final static Logger logger = LoggerFactory.getLogger(DelegatingUserDetailsService.class);
    static final String IDENTIFIER_ATTRIBUTE_HAKA = "urn:oid:1.3.6.1.4.1.5923.1.1.1.6";
    static final String IDENTIFIER_ATTRIBUTE_MPASSID = "urn:mpass.id:uid";

    private final String ENTITYID_MPASSID = "https://virkailija.hahtuvaopintopolku.fi/service-provider-app/saml/metadata/alias/mpassidtestsp";

    /* (non-Javadoc)
     * @see org.springframework.security.saml.userdetails.SAMLUserDetailsService#loadUserBySAML(org.springframework.security.saml.SAMLCredential)
     */
    @Override
    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
        UserDetailsDto userDetailsDto = new UserDetailsDto();
        if (ENTITYID_MPASSID.equals(credential.getLocalEntityID())) {
            userDetailsDto.setAuthenticationMethod("mpassid");
            userDetailsDto.setIdentifier(getUniqueIdentifier(credential, IDENTIFIER_ATTRIBUTE_MPASSID));
        } else {
            userDetailsDto.setAuthenticationMethod("haka");
            userDetailsDto.setIdentifier(getUniqueIdentifier(credential, IDENTIFIER_ATTRIBUTE_HAKA));
        }
        return userDetailsDto;
    }

    public static String getFirstAttributeValue(SAMLCredential credential, String attributeName) {
        Attribute attrib = null;
        for (Attribute attr : credential.getAttributes()) {
            if (attr.getName() != null && attr.getName().equalsIgnoreCase(attributeName)) {
                attrib = attr;
                break;
            }
        }

        if (attrib == null) {
            logger.debug("Could not find attribute {}", attributeName);
            return null;
        }

        XMLObject obj = attrib.getAttributeValues().get(0);
        if (obj instanceof XSString) {
            return ((XSString) obj).getValue();
        }
        if (obj instanceof XSAny) {
            return ((XSAny) obj).getTextContent();
        }

        logger.error("Could not parse field {} of type {}.", obj.getElementQName(), obj.getSchemaType());
        throw new SAMLCredentialsParseException("Could not parse field " + obj.getElementQName() + " of type "+ obj.getSchemaType());
    }

    private String getUniqueIdentifier(SAMLCredential credential, String attributeName) {
        String firstAttrValue = getFirstAttributeValue(credential, attributeName);
        if (firstAttrValue == null) {
            List<String> attrNames = new ArrayList<String>();
            for (Attribute attr : credential.getAttributes()) {
                attrNames.add(attr.getFriendlyName());
            }
            String attrsString = StringUtils.join(attrNames, ",");
            logger.warn("Could not find matching attribute for name {}, \nall attributes [{}]", attributeName, attrsString);
            throw new RequiredSamlAttributeNotProvidedException(attributeName);
        }
        logger.info("Found attribute {} with value '{}'", attributeName, firstAttrValue);
        return firstAttrValue;
    }

}
