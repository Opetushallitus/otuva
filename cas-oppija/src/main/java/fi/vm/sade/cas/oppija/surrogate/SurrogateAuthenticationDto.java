package fi.vm.sade.cas.oppija.surrogate;

public class SurrogateAuthenticationDto {

    /* impersonator */
    public final String principalId;
    public final String nationalIdentificationNumber;
    public final String personOid;
    public final String personName;
    /* surrogate */
    public final String personId;
    public final String name;

    public SurrogateAuthenticationDto(String principalId, String nationalIdentificationNumber, String personOid,
                                      String personName, String personId, String name) {
        this.principalId = principalId;
        this.nationalIdentificationNumber = nationalIdentificationNumber;
        this.personOid = personOid;
        this.personName = personName;
        this.personId = personId;
        this.name = name;
    }

}
