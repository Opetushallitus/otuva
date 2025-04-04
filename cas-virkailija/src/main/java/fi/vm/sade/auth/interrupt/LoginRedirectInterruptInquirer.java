package fi.vm.sade.auth.interrupt;

import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

@Component
public class LoginRedirectInterruptInquirer implements InterruptInquirer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginRedirectInterruptInquirer.class);

    private final KayttooikeusRestClient kayttooikeusRestClient;
    private final LoginRedirectUrlGenerator loginRedirectUrlGenerator;
    private boolean requireStrongIdentification;
    private List<String> requireStrongIdentificationUsernameList = new ArrayList<>();
    private boolean emailVerificationEnabled;
    private List<String> emailVerificationUsernameList = new ArrayList<>();

    public LoginRedirectInterruptInquirer(KayttooikeusRestClient kayttooikeusRestClient, LoginRedirectUrlGenerator loginRedirectUrlGenerator) {
        this.kayttooikeusRestClient = kayttooikeusRestClient;
        this.loginRedirectUrlGenerator = loginRedirectUrlGenerator;
    }

    @Override
    public InterruptResponse inquire(Authentication authentication, RegisteredService registeredService, Service service, Credential credential, RequestContext requestContext) {
        Optional<String> hakaRegistrationToken = getPrincipalAttribute(authentication, "hakaRegistrationToken");
        if (hakaRegistrationToken.isPresent()) {
            return getInterruptResponseByUrl(loginRedirectUrlGenerator.createRegistrationUrl(hakaRegistrationToken.get()));
        }

        String username = authentication.getPrincipal().getId();
        Optional<String> idpEntityId = getPrincipalAttribute(authentication, "idpEntityId");
        return kayttooikeusRestClient.getRedirectCodeByUsername(username)
                .flatMap(redirectCode -> getRedirectUrl(redirectCode, username, idpEntityId))
                .map(this::getInterruptResponseByUrl)
                .orElseGet(InterruptResponse::none);
    }

    private Optional<String> getPrincipalAttribute(Authentication authentication, String attributeName) {
        if (authentication.getPrincipal().getAttributes() == null) {
            return Optional.empty();
        }
        List<Object> attribute = authentication.getPrincipal().getAttributes().get(attributeName);
        return attribute != null && attribute.size() > 0
            ? Optional.of((String) attribute.get(0))
            : Optional.empty();
    }

    private Optional<String> getRedirectUrl(String redirectCode, String username, Optional<String> idpEntityId) {
        switch (redirectCode) {
            case "STRONG_IDENTIFICATION":
                LOGGER.info("Strong identification interrupt received for {}", username);
                if (requireStrongIdentification || requireStrongIdentificationUsernameList.contains(username)) {
                    return Optional.of(loginRedirectUrlGenerator.createRedirectUrl(username, "henkilo-ui.strong-identification"));
                }
                break;
            case "EMAIL_VERIFICATION":
                LOGGER.info("Email verification interrupt received for {}", username);
                if (emailVerificationEnabled || emailVerificationUsernameList.contains(username)) {
                    return Optional.of(loginRedirectUrlGenerator.createRedirectUrl(username, "henkilo-ui.email-verification"));
                }
                break;
            case "PASSWORD_CHANGE":
                LOGGER.info("Password change interrupt received for {}", username);
                if (!idpEntityId.orElse("").equals("vetuma")) {
                    return Optional.of(loginRedirectUrlGenerator.createRedirectUrl(username, "henkilo-ui.password-change"));
                } else {
                    LOGGER.info("Bypassing password change for {} due to Suomi.fi", username);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown redirectCode: " + redirectCode);
        }
        return Optional.empty();
    }

    private InterruptResponse getInterruptResponseByUrl(String url) {
        InterruptResponse interruptResponse = new InterruptResponse();
        interruptResponse.setLinks(Map.of("redirect", url));
        interruptResponse.setInterrupt(true);
        interruptResponse.setBlock(true);
        interruptResponse.setAutoRedirect(true);
        return interruptResponse;
    }

    @Value("${require-strong-identification}")
    public void setRequireStrongIdentification(boolean requireStrongIdentification) {
        this.requireStrongIdentification = requireStrongIdentification;
    }

    @Value("#{'${require-strong-identification.usernamelist}'.split(',')}")
    public void setRequireStrongIdentificationUsernameList(List<String> requireStrongIdentificationUsernameList) {
        this.requireStrongIdentificationUsernameList = requireStrongIdentificationUsernameList.stream()
                .filter(not(String::isEmpty))
                .collect(toList());
    }

    @Value("${email-verification-enabled}")
    public void setEmailVerificationEnabled(boolean emailVerificationEnabled) {
        this.emailVerificationEnabled = emailVerificationEnabled;
    }

    @Value("#{'${email-verification-enabled.usernamelist}'.split(',')}")
    public void setEmailVerificationUsernameList(List<String> emailVerificationUsernameList) {
        this.emailVerificationUsernameList = emailVerificationUsernameList.stream()
                .filter(not(String::isEmpty))
                .collect(toList());
    }

}
