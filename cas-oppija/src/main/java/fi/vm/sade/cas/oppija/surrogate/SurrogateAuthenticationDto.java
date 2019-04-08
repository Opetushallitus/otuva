package fi.vm.sade.cas.oppija.surrogate;

public class SurrogateAuthenticationDto {

    /* impersonator */
    public final SurrogateImpersonatorData impersonatorData;
    /* surrogate */
    public final String nationalIdentificationNumber;
    public final String name;

    public SurrogateAuthenticationDto(SurrogateImpersonatorData impersonatorData, String nationalIdentificationNumber, String name) {
        this.impersonatorData = impersonatorData;
        this.nationalIdentificationNumber = nationalIdentificationNumber;
        this.name = name;
    }

}
