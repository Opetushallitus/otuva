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
import static org.springframework.util.StringUtils.capitalize;

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
            SurrogateAuthenticationDto dto = surrogateService.getAuthentication(credential.getToken(), credential.getCode());
            credential.setAuthenticationAttributes(dto.impersonatorData.authenticationAttributes);
            return createHandlerResult(credential, createPrincipal(dto));
        } catch (GeneralSecurityException e) {
            LOGGER.warn(e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new PreventedException(e);
        }
    }

    private Principal createPrincipal(SurrogateAuthenticationDto dto) {
        String id = dto.impersonatorData.principalId + UserProfile.SEPARATOR + dto.nationalIdentificationNumber;
        Map<String, Object> attributes = new LinkedHashMap<>();
        dto.impersonatorData.principalAttributes.entrySet().stream()
                .forEach(entry -> attributes.put(impersonatorAttributeKey(entry.getKey()), entry.getValue()));
        attributes.put(ATTRIBUTE_NAME_NATIONAL_IDENTIFICATION_NUMBER, dto.nationalIdentificationNumber);
        try {
            personService.findOidByNationalIdentificationNumber(dto.nationalIdentificationNumber)
                    .ifPresent(oid -> attributes.put(ATTRIBUTE_NAME_PERSON_OID, oid));
        } catch (Exception e) {
            LOGGER.error("Unable to get oid by national identification number", e);
        }
        attributes.put(ATTRIBUTE_NAME_PERSON_NAME, dto.name);
        return principalFactory.createPrincipal(id, attributes);
    }

    private static String impersonatorAttributeKey(String key) {
        return String.format("impersonator%s", capitalize(key));
    }

    private AuthenticationHandlerExecutionResult createHandlerResult(SurrogateCredential credential, Principal principal) {
        return new DefaultAuthenticationHandlerExecutionResult(this, new BasicCredentialMetaData(credential), principal);
    }

}
