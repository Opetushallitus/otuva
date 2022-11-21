package fi.vm.sade.cas.oppija.surrogate;

import org.apereo.cas.authentication.Credential;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class SurrogateCredential implements Credential {

    private final String token;
    private final String code;
    private Map<String, List<Object>> authenticationAttributes;

    public SurrogateCredential(String token, String code) {
        this.token = requireNonNull(token);
        this.code = requireNonNull(code);
    }

    @Override
    public String getId() {
        return token;
    }


    public String getCode() {
        return code;
    }

    public Map<String, List<Object>> getAuthenticationAttributes() {
        return authenticationAttributes;
    }

    public void setAuthenticationAttributes(Map<String, List<Object>> authenticationAttributes) {
        this.authenticationAttributes = authenticationAttributes;
    }

}
