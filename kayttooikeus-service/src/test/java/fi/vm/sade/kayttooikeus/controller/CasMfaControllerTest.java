package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.model.GoogleAuthToken;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

@RunWith(SpringRunner.class)
public class CasMfaControllerTest extends AbstractControllerTest {
    @MockBean
    private KayttajatiedotService kayttajatiedotService;
    private String username = "cas";
    private String password = "mfa";

    @Test
    public void getMfaTriggerRequiresBasicAuth() throws Exception {
        when(kayttajatiedotService.getMfaProvider(any())).thenReturn(Optional.of("mfa-gauth"));
        mvc.perform(post("/mfa/trigger")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"principalId\":\"username\",\"serviceId\":\"service\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void getMfaTriggerReturnsMfaProvider() throws Exception {
        when(kayttajatiedotService.getMfaProvider(any())).thenReturn(Optional.of("mfa-gauth"));
        mvc.perform(post("/mfa/trigger")
            .with(httpBasic(username, password))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"principalId\":\"username\",\"serviceId\":\"service\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string("mfa-gauth"));
    }

    @Test
    public void getMfaTriggerReturnsEmptyStringIfNoMfaProvider() throws Exception {
        when(kayttajatiedotService.getMfaProvider(any())).thenReturn(Optional.empty());
        mvc.perform(post("/mfa/trigger")
            .with(httpBasic(username, password))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"principalId\":\"username\",\"serviceId\":\"service\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }

    @Test
    public void getGoogleAuthTokenRequiresBasicAuth() throws Exception {
        when(kayttajatiedotService.getGoogleAuthToken(any())).thenReturn(Optional.empty());
        mvc.perform(get("/mfa/token")
            .header("username", "username")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void getGoogleAuthTokenReturns404WhenTokenNotFound() throws Exception {
        when(kayttajatiedotService.getGoogleAuthToken(any())).thenReturn(Optional.empty());
        mvc.perform(get("/mfa/token")
            .with(httpBasic(username, password))
            .header("username", "username")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void getGoogleAuthTokenReturnsToken() throws Exception {
        var token = new GoogleAuthToken();
        token.setId(1);
        token.setName("laite");
        token.setScratchCodes(new Integer[]{1,2,3});
        token.setRegistrationDate(LocalDateTime.of(1979, Month.APRIL, 2, 3, 4, 0, 0));
        token.setSecretKey("ADADFADAAADFADAFADFA");
        token.setValidationCode(124356l);
        when(kayttajatiedotService.getGoogleAuthToken(any())).thenReturn(Optional.of(token));
        mvc.perform(get("/mfa/token")
            .with(httpBasic(username, password))
            .header("username", "username")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("[\"java.util.ArrayList\",[{\"scratchCodes\":[\"java.util.ArrayList\",[1,2,3]],\"id\":1,\"secretKey\":\"ADADFADAAADFADAFADFA\",\"validationCode\":124356,\"username\":\"username\",\"name\":\"laite\",\"registrationDate\":\"1979-04-02T03:04\",\"@class\":\"org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount\"}]]"));
    }
}
