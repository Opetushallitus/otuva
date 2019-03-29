package fi.vm.sade.cas.oppija.surrogate;

import java.io.Serializable;
import java.time.Instant;

public class SurrogateSession implements Serializable {

    public final Instant created;
    public final String nationalIdentificationNumber;
    public final String principalId;
    public final String personOid;
    public final String personName;
    public final String language;

    public String redirectUrl;
    public String requestId;
    public String sessionId;
    public String userId;

    public SurrogateSession(String nationalIdentificationNumber, String principalId, String personOid,
                            String personName, String language) {
        this(Instant.now(), nationalIdentificationNumber, principalId, personOid, personName, language);
    }

    public SurrogateSession(Instant created, String nationalIdentificationNumber, String principalId, String personOid,
                            String personName, String language) {
        this.created = created;
        this.nationalIdentificationNumber = nationalIdentificationNumber;
        this.principalId = principalId;
        this.personOid = personOid;
        this.personName = personName;
        this.language = language;
    }

    public void update(String redirectUrl, String requestId, String sessionId, String userId) {
        this.redirectUrl = redirectUrl;
        this.requestId = requestId;
        this.sessionId = sessionId;
        this.userId = userId;
    }

}
