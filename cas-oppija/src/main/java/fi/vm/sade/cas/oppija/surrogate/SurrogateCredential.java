package fi.vm.sade.cas.oppija.surrogate;

import org.apereo.cas.authentication.Credential;

import static java.util.Objects.requireNonNull;

public class SurrogateCredential implements Credential {

    private final String token;
    private final String code;

    public SurrogateCredential(String token, String code) {
        this.token = requireNonNull(token);
        this.code = code;
    }

    @Override
    public String getId() {
        return token;
    }

    public String getToken() {
        return token;
    }

    public String getCode() {
        return code;
    }

}
