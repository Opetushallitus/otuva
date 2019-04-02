package fi.vm.sade.cas.oppija.surrogate;

import java.util.Map;

public class SurrogateAuthenticationDto {

    /* impersonator */
    public final String principalId;
    public final Map<String, Object> principalAttributes;
    public final Map<String, Object> authenticationAttributes;
    /* surrogate */
    public final String personId;
    public final String name;

    public SurrogateAuthenticationDto(String principalId, Map<String, Object> principalAttributes,
                                      Map<String, Object> authenticationAttributes, String personId, String name) {
        this.principalId = principalId;
        this.principalAttributes = principalAttributes;
        this.authenticationAttributes = authenticationAttributes;
        this.personId = personId;
        this.name = name;
    }

}
