package fi.vm.sade.cas.oppija.surrogate.interrupt;

import fi.vm.sade.cas.oppija.surrogate.SurrogateCredential;
import fi.vm.sade.cas.oppija.surrogate.SurrogateImpersonatorData;
import fi.vm.sade.cas.oppija.surrogate.SurrogateService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.webflow.execution.RequestContext;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.*;
import static fi.vm.sade.cas.oppija.CasOppijaUtils.resolveAttribute;
import static fi.vm.sade.cas.oppija.surrogate.SurrogateConstants.TOKEN_PARAMETER_NAME;

@Component
@ConditionalOnProperty("valtuudet.enabled")
public class SurrogateInterruptInquirer implements InterruptInquirer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateInterruptInquirer.class);

    private final SurrogateService surrogateService;
    private final Environment environment;

    public SurrogateInterruptInquirer(SurrogateService surrogateService, Environment environment) {
        this.surrogateService = surrogateService;
        this.environment = environment;
    }

    @Override
    public InterruptResponse inquire(Authentication authentication, RegisteredService registeredService, Service service, Credential credential, RequestContext requestContext) {
        // user is already authenticating as surrogate
        if (SurrogateCredential.class.isInstance(credential)) {
            return InterruptResponse.none();
        }
        String serviceUrl = service != null ? service.getId() : null;
        String language = Optional.ofNullable(requestContext.getExternalContext().getLocale())
                .map(Locale::getLanguage)
                .filter(SUPPORTED_LANGUAGES::contains)
                .orElse(DEFAULT_LANGUAGE);
        return inquire(authentication, serviceUrl, language);
    }

    private InterruptResponse inquire(Authentication authentication, String serviceUrl, String language) {
        Principal principal = authentication.getPrincipal();
        Map<String, Object> principalAttributes = principal.getAttributes();
        Map<String, Object> authenticationAttributes = authentication.getAttributes();

        String nationalIdentificationNumber = resolveAttribute(principalAttributes,
                ATTRIBUTE_NAME_NATIONAL_IDENTIFICATION_NUMBER, String.class)
                .orElseThrow(() -> new IllegalArgumentException("National identification number not available"));

        SurrogateImpersonatorData impersonatorData = new SurrogateImpersonatorData(principal.getId(),
                principalAttributes, authenticationAttributes);
        String authorizeUrl = surrogateService.getAuthorizeUrl(nationalIdentificationNumber, language, impersonatorData,
                token -> createRedirectUrl(serviceUrl, token));

        InterruptResponse interruptResponse = new InterruptResponse();
        interruptResponse.setLinks(Map.of("Suomi.fi-valtuudet", authorizeUrl));
        boolean required = environment.getRequiredProperty("valtuudet.required", Boolean.class);
        interruptResponse.setBlock(required);
        interruptResponse.setAutoRedirect(required);
        return interruptResponse;
    }

    private String createRedirectUrl(String serviceUrl, String token) {
        UriComponentsBuilder redirectUrlBuilder = UriComponentsBuilder
                .fromHttpUrl(environment.getRequiredProperty("cas.server.prefix") + "/login")
                .queryParam(TOKEN_PARAMETER_NAME, token);
        if (serviceUrl != null) {
            redirectUrlBuilder.queryParam("service", serviceUrl);
        }
        return redirectUrlBuilder.toUriString();
    }

}
