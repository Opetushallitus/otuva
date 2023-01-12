package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.config.properties.CasProperties;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

@RunWith(SpringRunner.class)
public class MfaControllerTest extends AbstractControllerTest {
    @MockBean
    private KayttajatiedotService kayttajatiedotService;

    @Autowired
    private CasProperties properties;
  
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
            .with(httpBasic(properties.getMfa().getUsername(), properties.getMfa().getPassword()))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"principalId\":\"username\",\"serviceId\":\"service\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string("mfa-gauth"));
    }

    @Test
    public void getMfaTriggerReturnsEmptyStringIfNoMfaProvider() throws Exception {
        when(kayttajatiedotService.getMfaProvider(any())).thenReturn(Optional.empty());
        mvc.perform(post("/mfa/trigger")
            .with(httpBasic(properties.getMfa().getUsername(), properties.getMfa().getPassword()))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"principalId\":\"username\",\"serviceId\":\"service\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }
}
