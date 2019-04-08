package fi.vm.sade.cas.oppija.surrogate;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

public class SurrogateRequestData implements Serializable {

    public String redirectUrl;
    public String requestId;
    public String sessionId;

    public SurrogateRequestData(String redirectUrl, String requestId, String sessionId) {
        this.redirectUrl = requireNonNull(redirectUrl);
        this.requestId = requireNonNull(requestId);
        this.sessionId = requireNonNull(sessionId);
    }

}
