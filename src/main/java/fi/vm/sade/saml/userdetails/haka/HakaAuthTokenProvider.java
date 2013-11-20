/**
 * 
 */
package fi.vm.sade.saml.userdetails.haka;

import fi.vm.sade.authentication.service.types.AddHenkiloToOrganisaatiosDataType;
import fi.vm.sade.authentication.service.types.dto.HenkiloType;
import fi.vm.sade.authentication.service.types.dto.HenkiloTyyppiType;
import fi.vm.sade.saml.exception.UnregisteredHakaUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.SAMLCredential;

import fi.vm.sade.saml.userdetails.AbstractIdpBasedAuthTokenProvider;
import fi.vm.sade.saml.userdetails.model.IdentityData;

/**
 * @author tommiha
 * 
 */
public class HakaAuthTokenProvider extends AbstractIdpBasedAuthTokenProvider {

    public static final String HAKA_IDP_ID = "haka";

    @Override
    protected String getIDPUniqueKey() {
        return HAKA_IDP_ID;
    }

    @Override
    protected String getUniqueIdentifier(SAMLCredential credential) {
        String eduPersonPrincipalName = getFirstAttributeValue(credential, "eduPersonPrincipalName");
        eduPersonPrincipalName = eduPersonPrincipalName.replace('@', '-');
        return eduPersonPrincipalName;
    }

    @Override
    protected IdentityData createIdentity(SAMLCredential credential) {
        IdentityData henkilo = new IdentityData();

        String nimi = getFirstAttributeValue(credential, "givenName");

        if(nimi == null || "".equals(nimi)) {
            nimi = getFirstAttributeValue(credential, "displayName");
        }

        henkilo.setEtunimet(nimi);
        henkilo.setSukunimi(getFirstAttributeValue(credential, "sn"));
        henkilo.setKutsumanimi(nimi);
        henkilo.setKayttajatunnus(getUniqueIdentifier(credential));

        henkilo.setDomainNimi(getFirstAttributeValue(credential, "schacHomeOrganization"));
        henkilo.setHenkiloTyyppi(HenkiloTyyppiType.VIRKAILIJA);

        logger.info("Creating henkilo data: {}", henkilo);

        return henkilo;
    }

    @Override
    protected AddHenkiloToOrganisaatiosDataType fillExtraPersonData(SAMLCredential credential, AddHenkiloToOrganisaatiosDataType henkiloData) {
        // Fill extra fields
        return henkiloData;
    }



    @Override
    public String createAuthenticationToken(SAMLCredential credential) {
        HenkiloType henkilo = getServiceProviderService().getHenkiloByIDPAndIdentifier(getIDPUniqueKey(),
                getUniqueIdentifier(credential));
        // TODO: This is just temp solution for release 7.0 (november 2013)
        // Prevents from new users from registering through HAKA_IDP_ID
        if (henkilo == null) {
            String eppn = getFirstAttributeValue(credential, "eduPersonPrincipalName");
            logger.info("Authentication denied for an unregistered Haka user: {}", eppn);
            throw new UnregisteredHakaUserException("Authentication denied for an unregistered Haka user: " + eppn);
        }
        // end of temp solution
        return super.createAuthenticationToken(credential);
    }
}
