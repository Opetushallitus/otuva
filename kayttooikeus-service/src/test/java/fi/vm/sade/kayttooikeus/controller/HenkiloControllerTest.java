package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.KayttajatiedotCreateDto;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class HenkiloControllerTest extends AbstractControllerTest {

    @MockBean
    private KayttajatiedotService kayttajatiedotService;

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void postHenkiloKayttajatiedotShouldReturnOk() throws Exception {
        mvc.perform(post("/henkilo/{henkiloOid}/kayttajatiedot", "1.2.3.4.5")
                .content("{\"username\": \"user1\"}").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        ArgumentCaptor<KayttajatiedotCreateDto> captor = ArgumentCaptor.forClass(KayttajatiedotCreateDto.class);
        verify(kayttajatiedotService).create(eq("1.2.3.4.5"), captor.capture());
        KayttajatiedotCreateDto kayttajatiedot = captor.getValue();
        assertThat(kayttajatiedot.getUsername()).isEqualTo("user1");
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void postHenkiloKayttajatiedotShouldReturnValidationError() throws Exception {
        mvc.perform(post("/henkilo/{henkiloOid}/kayttajatiedot", "1.2.3.4.5")
                .content("{\"username\": \"user.1\"}").contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("Pattern")));
        verifyZeroInteractions(kayttajatiedotService);
    }

}
