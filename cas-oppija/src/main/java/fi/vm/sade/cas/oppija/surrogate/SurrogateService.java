package fi.vm.sade.cas.oppija.surrogate;

import java.security.GeneralSecurityException;
import java.util.function.Function;

public interface SurrogateService {

    String getAuthorizeUrl(String nationalIdentificationNumber, String language,
                           SurrogateImpersonatorData impersonatorData, Function<String, String> tokenToRedirectUrl);

    SurrogateAuthenticationDto getAuthentication(String token, String code) throws GeneralSecurityException;

}
