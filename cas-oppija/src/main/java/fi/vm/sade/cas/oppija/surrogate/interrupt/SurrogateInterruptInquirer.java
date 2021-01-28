package fi.vm.sade.cas.oppija.surrogate.interrupt;

import fi.vm.sade.cas.oppija.surrogate.SurrogateCredential;
import fi.vm.sade.cas.oppija.surrogate.SurrogateImpersonatorData;
import fi.vm.sade.cas.oppija.surrogate.SurrogateService;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.Pac4jConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.*;
import static fi.vm.sade.cas.oppija.CasOppijaUtils.resolveAttribute;

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
            LOGGER.info("User is already authenticating as surrogate {}" + credential.getId());
            return InterruptResponse.none();
        }
        String language = Optional.ofNullable(requestContext.getExternalContext().getLocale())
                .map(Locale::getLanguage)
                .filter(SUPPORTED_LANGUAGES::contains)
                .orElse(DEFAULT_LANGUAGE);

        boolean isValtuudetEnabled = requestContext.getActiveFlow().getAttributes().contains("valtuudet")
                ? (Boolean) requestContext.getActiveFlow().getAttributes().get("valtuudet") : VALTUUDET_ENABLED;

        HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        String clientName = request.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);

        LOGGER.info("VALTUUDET | RequestContext contains valtuudet: {} | Valtuudet: {} | isValtuudetEnabled: {} | clientName: {} | Credential: {} | {}",
                requestContext.getActiveFlow().getAttributes().contains("valtuudet"),
                requestContext.getActiveFlow().getAttributes().get("valtuudet"),
                isValtuudetEnabled,
                clientName,
                credential.getId(),
                service);

        if (service.getId().contains("valtuudet=true")) {
            return inquire(authentication, service, language);
        } else {
            return InterruptResponse.none();
        }
    }

    private InterruptResponse inquire(Authentication authentication, Service service, String language) {
        Principal principal = authentication.getPrincipal();
        Map<String, Object> principalAttributes = principal.getAttributes();
        Map<String, Object> authenticationAttributes = authentication.getAttributes();

        String nationalIdentificationNumber = resolveAttribute(principalAttributes,
                ATTRIBUTE_NAME_NATIONAL_IDENTIFICATION_NUMBER, String.class)
                .orElseThrow(() -> new IllegalArgumentException("National identification number not available"));

        SurrogateImpersonatorData impersonatorData = new SurrogateImpersonatorData(principal.getId(),
                principalAttributes, authenticationAttributes);
        String redirectUrl = surrogateService.getRedirectUrl(service, nationalIdentificationNumber, language, impersonatorData);

        InterruptResponse interruptResponse = new InterruptResponse();
        interruptResponse.setLinks(Map.of("Suomi.fi-valtuudet", redirectUrl));
        boolean required = environment.getRequiredProperty("valtuudet.required", Boolean.class);
        interruptResponse.setBlock(required);
        interruptResponse.setAutoRedirect(required);
        return interruptResponse;
    }

}
