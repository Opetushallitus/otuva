package fi.vm.sade.kayttooikeus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.kayttooikeus.dto.VirkailijaCreateDto;
import fi.vm.sade.kayttooikeus.service.VirkailijaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
public class VirkailijaControllerTest extends AbstractControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private VirkailijaService virkailijaService;

    private VirkailijaCreateDto createValidCreateDto() {
        VirkailijaCreateDto createDto = new VirkailijaCreateDto();
        createDto.setEtunimet("etunimet");
        createDto.setKutsumanimi("kutsumanimi");
        createDto.setSukunimi("sukunimi");
        createDto.setKayttajatunnus("kayttajatunnus");
        createDto.setSalasana("salasana");
        return createDto;
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_VIRKAILIJANLUONTI")
    public void postBadRequest() throws Exception {
        VirkailijaCreateDto createDto = new VirkailijaCreateDto();
        when(virkailijaService.create(any())).thenReturn("oid123");

        this.mvc.perform(post("/virkailija")
                .content(objectMapper.writeValueAsString(createDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(virkailijaService);
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void postForbidden() throws Exception {
        VirkailijaCreateDto createDto = createValidCreateDto();
        when(virkailijaService.create(any())).thenReturn("oid123");

        this.mvc.perform(post("/virkailija")
                .content(objectMapper.writeValueAsString(createDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(anyOf(is(401), is(403))));

        verifyNoInteractions(virkailijaService);
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_VIRKAILIJANLUONTI")
    public void postOk() throws Exception {
        VirkailijaCreateDto createDto = createValidCreateDto();
        when(virkailijaService.create(any())).thenReturn("oid123");

        this.mvc.perform(post("/virkailija")
                .content(objectMapper.writeValueAsString(createDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<VirkailijaCreateDto> captor = ArgumentCaptor.forClass(VirkailijaCreateDto.class);
        verify(virkailijaService).create(captor.capture());
        assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(createDto);
    }

}
