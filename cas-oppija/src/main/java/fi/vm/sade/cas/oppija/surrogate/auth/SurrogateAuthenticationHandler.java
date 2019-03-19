package fi.vm.sade.cas.oppija.surrogate.auth;

import fi.vm.sade.cas.oppija.service.PersonService;
import fi.vm.sade.cas.oppija.surrogate.SurrogateAuthenticationDto;
import fi.vm.sade.cas.oppija.surrogate.SurrogateCredential;
import fi.vm.sade.cas.oppija.surrogate.SurrogateService;
import org.apereo.cas.authentication.*;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.*;

public class SurrogateAuthenticationHandler implements AuthenticationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateAuthenticationHandler.class);

    private final SurrogateService surrogateService;
    private final PersonService personService;
    private final PrincipalFactory principalFactory;

    public SurrogateAuthenticationHandler(SurrogateService surrogateService,
                                          PersonService personService,
                                          PrincipalFactory principalFactory) {
        this.surrogateService = surrogateService;
        this.personService = personService;
        this.principalFactory = principalFactory;
    }

    @Override
    public boolean supports(Credential credential) {
        return SurrogateCredential.class.isInstance(credential);
    }

    @Override
    public boolean supports(Class<? extends Credential> clazz) {
        return SurrogateCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public AuthenticationHandlerExecutionResult authenticate(Credential credential) throws GeneralSecurityException, PreventedException {
        return authenticate(SurrogateCredential.class.cast(credential));
    }

    public AuthenticationHandlerExecutionResult authenticate(SurrogateCredential credential) throws GeneralSecurityException, PreventedException {
        try {
            if (credential.getCode() == null) {
                throw new PreventedException("User cancelled surrogate authentication");
            }
            SurrogateAuthenticationDto dto = surrogateService.getAuthentication(credential.getToken(), credential.getCode());
            return createHandlerResult(credential, createPrincipal(dto));
        } catch (GeneralSecurityException e) {
            LOGGER.warn(e.getMessage());
            throw e;
        } catch (PreventedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreventedException(e);
        }
    }

    private Principal createPrincipal(SurrogateAuthenticationDto dto) {
        String id = dto.principalId + UserProfile.SEPARATOR + dto.personId;
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("impersonatorNationalIdentificationNumber", dto.nationalIdentificationNumber);
        if (dto.personOid != null) {
            attributes.put("impersonatorPersonOid", dto.personOid);
        }
        attributes.put("impersonatorPersonName", dto.personName);
        attributes.put(ATTRIBUTE_NAME_NATIONAL_IDENTIFICATION_NUMBER, dto.personId);
        personService.findOidByNationalIdentificationNumber(dto.personId)
                .ifPresent(oid -> attributes.put(ATTRIBUTE_NAME_PERSON_OID, oid));
        attributes.put(ATTRIBUTE_NAME_PERSON_NAME, dto.name);
        return principalFactory.createPrincipal(id, attributes);
    }

    private AuthenticationHandlerExecutionResult createHandlerResult(SurrogateCredential credential, Principal principal) {
        return new DefaultAuthenticationHandlerExecutionResult(this, new BasicCredentialMetaData(credential), principal);
    }

}
