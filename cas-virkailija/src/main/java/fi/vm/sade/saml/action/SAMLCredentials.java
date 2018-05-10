package fi.vm.sade.saml.action;

import fi.vm.sade.auth.dto.IdentifiedHenkiloType;
import org.jasig.cas.authentication.Credential;

public class SAMLCredentials implements Credential {

    private String token;
    private IdentifiedHenkiloType userDetails;

    public SAMLCredentials(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public IdentifiedHenkiloType getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(IdentifiedHenkiloType userDetails) {
        this.userDetails = userDetails;
    }

    @Override
    public String getId() {
        return token;
    }

}
