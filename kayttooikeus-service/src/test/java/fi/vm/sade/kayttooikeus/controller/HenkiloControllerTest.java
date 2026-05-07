package fi.vm.sade.kayttooikeus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

@Sql("/truncate_tables.sql")
@Sql("/test-data.sql")
@SpringBootTest
public class HenkiloControllerTest extends AbstractControllerTest {
    @MockitoBean
    private OrganisaatioHenkiloService service;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KayttajatiedotService kayttajatiedotService;

    @MockitoBean
    private HenkiloService henkiloService;

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = {"ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA", "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA_1.2.246.562.10.00000000001"})
    public void getByKayttajatunnus() throws Exception {
        given(this.henkiloService.getByKayttajatunnus(any())).willReturn(HenkiloReadDto.builder().oid("oid1").build());
        this.mvc.perform(get("/henkilo/kayttajatunnus=user1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().json("{\"oid\":\"oid1\"}"));
        verify(this.henkiloService).getByKayttajatunnus(eq("user1"));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = {"ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA", "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA_1.2.246.562.10.00000000001"})
    public void getHenkiloKayttajatiedotShouldReturnNotFoundError() throws Exception {
        when(kayttajatiedotService.getByHenkiloOid(any()))
                .thenThrow(NotFoundException.class);
        mvc.perform(get("/henkilo/{henkiloOid}/kayttajatiedot", "1.2.3.4.5"))
                .andExpect(status().isNotFound());
        verify(kayttajatiedotService).getByHenkiloOid(eq("1.2.3.4.5"));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = {"ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA", "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA_1.2.246.562.10.00000000001"})
    public void listOrganisatioHenkilosTest() throws Exception {
        given(this.service.listOrganisaatioHenkilos("1.2.3.4.5", "fi", null)).willReturn(singletonList(
                OrganisaatioHenkiloWithOrganisaatioDto.organisaatioBuilder().id(1L)
                        .passivoitu(false)
                        .organisaatio(OrganisaatioWithChildrenDto.builder()
                                .oid("1.2.3.4.7")
                                .nimi(new TextGroupMapDto().put("fi", "Suomeksi")
                                        .put("sv", "Ruotsiksi"))
                                .tyypit(singletonList("OPPILAITOS"))
                        .build())
                .build()
        ));
        this.mvc.perform(get("/henkilo/1.2.3.4.5/organisaatio").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content()
                .json(jsonResource("classpath:henkilo/henkiloOrganisaatios.json")));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = {"ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA", "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA_1.2.246.562.10.00000000001"})
    public void changePassword() throws Exception {
        mvc.perform(post("/henkilo/{henkiloOid}/password", "1.2.3.4.5")
                .content("\"1.2.3.4.5\"").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(kayttajatiedotService).changePasswordAsAdmin(eq("1.2.3.4.5"), eq("1.2.3.4.5"));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = {"ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA", "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA_1.2.246.562.10.00000000001"})
    public void changePasswordForm() throws Exception {
        mvc.perform(post("/henkilo/{henkiloOid}/password", "1.2.3.4.5")
                .content("Hacked=1").contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isUnsupportedMediaType());
        verifyNoInteractions(kayttajatiedotService);
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = {"ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA", "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA_1.2.246.562.10.00000000001"})
    public void changePasswordContentTypeUndefined() throws Exception {
        mvc.perform(post("/henkilo/{henkiloOid}/password", "1.2.3.4.5")
                .content("\"1.2.3.4.5\""))
                .andExpect(status().isUnsupportedMediaType());
        verifyNoInteractions(kayttajatiedotService);
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = {"ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA", "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA_1.2.246.562.10.00000000001"})
    public void changeUsernameReturnsBadRequestOnDuplicateUsername() throws Exception {
        given(this.kayttajatiedotService.updateKayttajatiedot(any(), any())).willThrow(new DataIntegrityViolationException("error"));
        mvc.perform(put("/henkilo/{oid}/kayttajatiedot", "1.2.3.4.6")
                .content("{\"username\": \"pirjo\"}").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", roles = {"APP_KAYTTOOIKEUS_REKISTERINPITAJA", "APP_KAYTTOOIKEUS_REKISTERINPITAJA_1.2.246.562.10.00000000001"})
    public void updatesAndGetHakaTunnus() throws Exception {

        String getOriginal = mvc.perform(get("/henkilo/1.2.246.562.24.37535704268/hakatunnus").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertHakaTunnukset(getOriginal);

        String putNewTunnukset = mvc.perform(put("/henkilo/1.2.246.562.24.37535704268/hakatunnus")
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
["hakatunus", "uustunus"]""")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertHakaTunnukset(putNewTunnukset, "hakatunus", "uustunus");

        String getUpdatedTunnukset = mvc.perform(get("/henkilo/1.2.246.562.24.37535704268/hakatunnus").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertHakaTunnukset(getUpdatedTunnukset, "hakatunus", "uustunus");
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268")
    public void updatesAndGetsOwnHakaTunnus() throws Exception {
        String getOriginal = mvc.perform(get("/henkilo/hakatunnus").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertHakaTunnukset(getOriginal);

        String putNewTunnukset = mvc.perform(put("/henkilo/hakatunnus")
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
["hakatunus", "uustunus"]""")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertHakaTunnukset(putNewTunnukset, "hakatunus", "uustunus");

        String getUpdatedTunnukset = mvc.perform(get("/henkilo/hakatunnus").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertHakaTunnukset(getUpdatedTunnukset, "hakatunus", "uustunus");
    }

    private void assertHakaTunnukset(String response, String... tunnukset) throws Exception {
        List<String> hakaTunnukset = objectMapper.readValue(response, TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
        assertThat(hakaTunnukset).containsExactlyInAnyOrder(tunnukset);
    }
}
