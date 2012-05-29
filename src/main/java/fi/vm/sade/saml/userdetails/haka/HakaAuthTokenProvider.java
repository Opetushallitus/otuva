/**
 * 
 */
package fi.vm.sade.saml.userdetails.haka;

import org.springframework.security.saml.SAMLCredential;

import fi.vm.sade.authentication.service.types.dto.HenkiloDTO;
import fi.vm.sade.saml.userdetails.AbstractIdpBasedAuthTokenProvider;

/**
 * @author tommiha
 *
 */
public class HakaAuthTokenProvider extends AbstractIdpBasedAuthTokenProvider {

    public static final String HAKA = "haka";
    
    @Override
    protected String getIDPUniqueKey() {
        return HAKA;
    }

    @Override
    protected String getUniqueIdentifier(SAMLCredential credential) {
        return getFirstAttributeValue(credential, "eduPersonPrincipalName");
    }

    @Override
    protected HenkiloDTO createIdentity(SAMLCredential credential) {
        HenkiloDTO henkilo = new HenkiloDTO();
        henkilo.setEtunimet(getFirstAttributeValue(credential, "givenName"));
        henkilo.setSukunimi(getFirstAttributeValue(credential, "sn"));
        henkilo.setKutsumanimi(getFirstAttributeValue(credential, "givenName"));
        henkilo.setHetu(getFirstAttributeValue(credential, "eduPersonPrincipalName"));
        return henkilo;
    }

}
