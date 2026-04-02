package fi.vm.sade.auth.cas;

import fi.vm.sade.auth.clients.KayttooikeusClient;
import fi.vm.sade.auth.clients.KayttooikeusOauth2Client;
import fi.vm.sade.saml.action.SAMLCredentials;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.multitenancy.TenantsManager;
import org.apereo.cas.web.UrlValidator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.security.auth.login.FailedLoginException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class HttpAuthenticationHandlerTest {
    AuthenticationHandler authenticationHandler;
    KayttooikeusClient kayttooikeusClient;

    @Before
    public void setup() throws IOException {
        KayttooikeusClient kayttooikeusClient = mock(KayttooikeusOauth2Client.class);
        authenticationHandler = new HttpAuthenticationHandler(0, kayttooikeusClient);
    }

    @Test
    public void handlerShouldHaveName() {
        assertThat(authenticationHandler.getName()).isNotNull();
    }

    @Test
    public void handlerShouldSupportUsernamePasswordCredential() {
        assertThat(authenticationHandler.supports(new UsernamePasswordCredential("user1", "pass1"))).isTrue();
    }

    @Test
    public void handlerShouldNotSupportSamlCredential() {
        assertThat(authenticationHandler.supports(new SAMLCredentials("token1"))).isFalse();
    }

    @Test
    public void authenticate() throws Throwable {
        when(kayttooikeusClient.getUserAttributesByUsernamePassword(any(), any()))
            .thenReturn(Optional.of(
                    new CasUserAttributes(
                        "1.2.246.562.24.74168788054",
                        "USER1",
                        List.of(),
                        Optional.empty(),
                        Optional.of("VIRKAILIJA"),
                        Optional.empty(),
                        List.of())));

        AuthenticationHandlerExecutionResult authenticate = authenticationHandler.authenticate(new UsernamePasswordCredential("user1", "pass1"), getService());

        assertThat(authenticate.getPrincipal().getId()).isEqualTo("USER1");
        List<Object> idpEntityIdList = authenticate.getPrincipal().getAttributes().get("idpEntityId");
        assertThat(idpEntityIdList).containsOnly("usernamePassword");
    }

    @Test
    public void defaultKayttajaTyyppiToVirkailijaIfMissing() throws Throwable {
        when(kayttooikeusClient.getUserAttributesByUsernamePassword(any(), any()))
            .thenReturn(Optional.of(
                    new CasUserAttributes(
                        "1.2.246.562.24.74168788054",
                        "USER1",
                        List.of(),
                        Optional.empty(),
                        Optional.of("VIRKAILIJA"),
                        Optional.empty(),
                        List.of())));

        AuthenticationHandlerExecutionResult authenticate = authenticationHandler.authenticate(new UsernamePasswordCredential("user1", "pass1"), getService());

        assertThat(authenticate.getPrincipal().getId()).isEqualTo("USER1");
        List<Object> idpEntityIdList = authenticate.getPrincipal().getAttributes().get("idpEntityId");
        assertThat(authenticate.getPrincipal().getAttributes().get("kayttajaTyyppi").get(0)).isEqualTo("VIRKAILIJA");
        assertThat(idpEntityIdList).containsOnly("usernamePassword");
    }

    @Test
    public void authenticateShouldThrowFailedLoginException() {
        when(kayttooikeusClient.getUserAttributesByUsernamePassword(any(), any())).thenReturn(Optional.empty());

        Throwable throwable = catchThrowable(() -> authenticationHandler.authenticate(new UsernamePasswordCredential("user1", "pass1"), getService()));

        assertThat(throwable).isInstanceOf(FailedLoginException.class);
    }

    @Test
    public void authenticateShouldThrowPreventedException() {
        when(kayttooikeusClient.getUserAttributesByUsernamePassword(any(), any())).thenThrow(new RuntimeException("json parse exception"));

        Throwable throwable = catchThrowable(() -> authenticationHandler.authenticate(new UsernamePasswordCredential("user1", "pass1"), getService()));

        assertThat(throwable).isInstanceOf(PreventedException.class);
    }

    private static AbstractWebApplicationService getService() {
        String serviceName = "http://example.com";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, serviceName);
        return (AbstractWebApplicationService) new WebApplicationServiceFactory(new TenantExtractor() {

            @Override
            public TenantsManager getTenantsManager() {
                return null;
            }

            @Override
            public ApplicationContext getApplicationContext() {
                return null;
            }

            @Override
            public Optional<TenantDefinition> extract(String requestPath) {
                return Optional.empty();
            }

            @Override
            public String getTenantKey(TenantDefinition tenantDefinition) {
                return "";
            }

        }, new UrlValidator() {
            @Override
            public boolean isValid(String value) {
                return false;
            }

            @Override
            public boolean isValidDomain(String value) {
                return false;
            }
        }).createService(request);
    }
}
