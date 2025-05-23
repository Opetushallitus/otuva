package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.service.KayttajarooliProvider;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
public class UserDetailsControllerTest extends AbstractControllerTest {

    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private KayttajarooliProvider kayttajaRooliProvider;
    @MockitoBean
    private KayttajatiedotService kayttajatiedotService;

    @Test
    public void getUserDetailsReturnsOkWithoutAuthentication() throws Exception {
        mvc.perform(get("/userDetails/user1"))
                .andExpect(status().isOk());
    }

    @Test
    public void getByUsernameAndPasswordReturnsUnauthorizedWithWrongUserPass() throws Exception {
        when(kayttajatiedotService.getByUsernameAndPassword(any(), any())).thenThrow(UnauthorizedException.class);
        mvc.perform(post("/userDetails")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user1\",\"password\":\"pass1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getByUsernameAndPasswordReturnsBadRequestWithoutUsername() throws Exception {
        mvc.perform(post("/userDetails")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":null,\"password\":\"pass1\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(kayttajatiedotService);
    }

    @Test
    public void getByUsernameAndPasswordReturnsBadRequestWithoutPassword() throws Exception {
        mvc.perform(post("/userDetails")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user1\",\"password\":null}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(kayttajatiedotService);
    }

}
