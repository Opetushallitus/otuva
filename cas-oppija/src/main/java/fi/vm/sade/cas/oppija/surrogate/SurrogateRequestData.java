package fi.vm.sade.cas.oppija.surrogate;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

public class SurrogateRequestData implements Serializable {

    public String redirectUrl;
    public String sessionId;

    public SurrogateRequestData(String redirectUrl, String sessionId) {
        this.redirectUrl = requireNonNull(redirectUrl);
        this.sessionId = requireNonNull(sessionId);
    }

}
