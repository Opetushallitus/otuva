package fi.vm.sade.auth.interrupt;

import fi.vm.sade.auth.clients.KayttooikeusClient;
import fi.vm.sade.auth.clients.KayttooikeusOauth2Client;
import fi.vm.sade.auth.clients.OppijanumerorekisteriClient;
import fi.vm.sade.auth.clients.OppijanumerorekisteriOauth2Client;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.interrupt.InterruptResponse;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LoginRedirectInterruptInquirerTest {
    final PrincipalFactory principalFactory = PrincipalFactoryUtils.newPrincipalFactory();

    LoginRedirectInterruptInquirer inquirer;
    KayttooikeusClient kayttooikeusClientMock;
    OppijanumerorekisteriClient oppijanumerorekisteriClientMock;

    @Before
    public void setup() {
        kayttooikeusClientMock = mock(KayttooikeusOauth2Client.class);
        when(kayttooikeusClientMock.createLoginToken(any())).thenReturn("loginToken1");
        oppijanumerorekisteriClientMock = mock(OppijanumerorekisteriOauth2Client.class);
        when(oppijanumerorekisteriClientMock.getAsiointikieli(any())).thenReturn("fi");
        inquirer = new LoginRedirectInterruptInquirer(kayttooikeusClientMock, oppijanumerorekisteriClientMock, "https://localhost:8082/henkilo-ui/");
    }

    @Test
    public void onRedirectCodeNullShouldNotRedirect() throws Throwable {
        when(kayttooikeusClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.empty());
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(),emptyMap(),emptyMap(),emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        inquirer.setRequireStrongIdentification(true);
        inquirer.setEmailVerificationEnabled(true);

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response).returns(false, InterruptResponse::isInterrupt);
        verify(kayttooikeusClientMock).getRedirectCodeByUsername("user1");
        verifyNoMoreInteractions(kayttooikeusClientMock);
    }

    @Test
    public void onRequireStrongIdentificationAndStrongIdentificationRedirectCodeShouldRedirectToStrongIdentification() throws Throwable {
        when(kayttooikeusClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("STRONG_IDENTIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(),emptyMap(),emptyMap(),emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        inquirer.setRequireStrongIdentification(true);

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response)
                .returns(true, InterruptResponse::isInterrupt)
                .returns("https://localhost:8082/henkilo-ui/kayttaja/vahvatunnistusinfo/fi/loginToken1", t -> String.join(",",
                        t.getLinks().values()));
        verify(kayttooikeusClientMock).getRedirectCodeByUsername("user1");
    }

    @Test
    public void onUsernameInCasRequireStrongidentificationListShouldRedirectToStrongAuthentication() throws Throwable {
        when(kayttooikeusClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("STRONG_IDENTIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(),emptyMap(),emptyMap(),emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        inquirer.setRequireStrongIdentificationUsernameList(asList("user1", "user2"));

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response)
                .returns(true, InterruptResponse::isInterrupt)
                .returns("https://localhost:8082/henkilo-ui/kayttaja/vahvatunnistusinfo/fi/loginToken1", t -> String.join(",",
                        t.getLinks().values()));
        verify(kayttooikeusClientMock).getRedirectCodeByUsername("user1");
    }

    @Test
    public void onEmailVerificationEnabledAndEmailVerificationRedirectCodeShouldRedirectToEmailVerification() throws Throwable {
        when(kayttooikeusClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("EMAIL_VERIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(),emptyMap(),emptyMap(),emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        inquirer.setEmailVerificationEnabled(true);

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response)
                .returns(true, InterruptResponse::isInterrupt)
                .returns("https://localhost:8082/henkilo-ui/kayttaja/sahkopostivarmistus/fi/loginToken1", t -> String.join(","
                        , t.getLinks().values()));
        verify(kayttooikeusClientMock).getRedirectCodeByUsername("user1");
    }

    @Test
    public void onEmailVerificationDisabledAndUsernameInEmailVerificationListShouldRedirectToEmailVerification() throws Throwable {
        when(kayttooikeusClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("EMAIL_VERIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(),emptyMap(),emptyMap(),emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        inquirer.setEmailVerificationUsernameList(asList("user1", "user2"));

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response)
                .returns(true, InterruptResponse::isInterrupt)
                .returns("https://localhost:8082/henkilo-ui/kayttaja/sahkopostivarmistus/fi/loginToken1", t -> String.join(","
                        , t.getLinks().values()));
        verify(kayttooikeusClientMock).getRedirectCodeByUsername("user1");
    }

    @Test
    public void onStrongIdentificationAndEmailVerificationDisabledShouldNotRedirectWithStrongIdentification() throws Throwable {
        when(kayttooikeusClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("STRONG_IDENTIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(),emptyMap(),emptyMap(),emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response).returns(false, InterruptResponse::isInterrupt);
        verify(kayttooikeusClientMock).getRedirectCodeByUsername("user1");
        verifyNoMoreInteractions(kayttooikeusClientMock);
    }

    @Test
    public void onStrongIdentificationAndEmailVerificationDisabledShouldNotRedirectWithEmailVerification() throws Throwable {
        when(kayttooikeusClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("EMAIL_VERIFICATION"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(),emptyMap(),emptyMap(),emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");

        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response).returns(false, InterruptResponse::isInterrupt);
        verify(kayttooikeusClientMock).getRedirectCodeByUsername("user1");
        verifyNoMoreInteractions(kayttooikeusClientMock);
    }

    @Test
    public void onPasswordChangeInterrupt() throws Throwable {
        when(kayttooikeusClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("PASSWORD_CHANGE"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1"), emptyList(), emptyList(), emptyMap(), emptyMap(), emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response)
                .returns(true, InterruptResponse::isInterrupt)
                .returns("https://localhost:8082/henkilo-ui/kayttaja/salasananvaihto/fi/loginToken1", t -> String.join(",", t.getLinks().values()));
        verify(kayttooikeusClientMock).getRedirectCodeByUsername("user1");
    }

    @Test
    public void onPasswordChangeInterruptWithSuomiFi() throws Throwable {
        Map<String, List<Object>> suomiFiIdpAttributes = Map.of("idpEntityId", List.of("vetuma"));
        when(kayttooikeusClientMock.getRedirectCodeByUsername(any())).thenReturn(Optional.of("PASSWORD_CHANGE"));
        Authentication authentication = new DefaultAuthentication(ZonedDateTime.now(), principalFactory.createPrincipal("user1", suomiFiIdpAttributes), emptyList(), emptyList(), emptyMap(), emptyMap(), emptyMap());
        Credential credential = new UsernamePasswordCredential("user1", "pass1");
        InterruptResponse response = inquirer.inquire(authentication, null, null, credential, null);

        assertThat(response).returns(false, InterruptResponse::isInterrupt);
        verify(kayttooikeusClientMock).getRedirectCodeByUsername("user1");
        verifyNoMoreInteractions(kayttooikeusClientMock);
    }
}
