package fi.vm.sade.cas.oppija.surrogate;

import java.io.Serializable;
import java.util.Map;

public class SurrogateSession implements Serializable {

    public final String principalId;
    public final Map<String, Object> principalAttributes;
    public final Map<String, Object> authenticationAttributes;

    public String redirectUrl;
    public String requestId;
    public String sessionId;
    public String userId;

    public SurrogateSession(String principalId, Map<String, Object> principalAttributes, Map<String, Object> authenticationAttributes) {
        this.principalId = principalId;
        this.principalAttributes = principalAttributes;
        this.authenticationAttributes = authenticationAttributes;
    }

    public void update(String redirectUrl, String requestId, String sessionId, String userId) {
        this.redirectUrl = redirectUrl;
        this.requestId = requestId;
        this.sessionId = sessionId;
        this.userId = userId;
    }

}
