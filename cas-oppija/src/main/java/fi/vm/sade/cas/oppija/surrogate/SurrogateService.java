package fi.vm.sade.cas.oppija.surrogate;

import org.apereo.cas.authentication.principal.Service;

import java.security.GeneralSecurityException;

public interface SurrogateService {

    String getRedirectUrl(Service service, String nationalIdentificationNumber, String language,
                          SurrogateImpersonatorData impersonatorData);

    SurrogateAuthenticationDto getAuthentication(String token, String code) throws GeneralSecurityException;

}
