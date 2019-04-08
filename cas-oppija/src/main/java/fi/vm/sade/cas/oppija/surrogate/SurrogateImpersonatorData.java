package fi.vm.sade.cas.oppija.surrogate;

import java.io.Serializable;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class SurrogateImpersonatorData implements Serializable {

    public final String principalId;
    public final Map<String, Object> principalAttributes;
    public final Map<String, Object> authenticationAttributes;

    public SurrogateImpersonatorData(String principalId, Map<String, Object> principalAttributes, Map<String, Object> authenticationAttributes) {
        this.principalId = requireNonNull(principalId);
        this.principalAttributes = requireNonNull(principalAttributes);
        this.authenticationAttributes = requireNonNull(authenticationAttributes);
    }

}
