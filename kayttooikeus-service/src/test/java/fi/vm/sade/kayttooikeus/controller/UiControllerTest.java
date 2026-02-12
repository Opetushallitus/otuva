package fi.vm.sade.kayttooikeus.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

@Sql("/truncate_tables.sql")
@Sql("/virkailijahaku.sql")
@SpringBootTest
@AutoConfigureMockMvc
class UiControllerTest {
    @Autowired
    protected MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private OrganisaatioClient organisaatioClient;

    @BeforeEach
    private void beforeEach() {
        when(organisaatioClient.getOrganisaatioPerustiedotCached(any())).thenReturn(Optional.empty());
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268")
    public void virkailijahakuRequiresKayttooikeus() throws Exception {
        mvc.perform(post("/internal/virkailijahaku")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nameQuery\":\"wat\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void virkailijahakuRequiresValidCriteria() throws Exception {
        mvc.perform(post("/internal/virkailijahaku")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/internal/virkailijahaku")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
    {"nameQuery":"aa"}""")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void virkailijahakuDoesNotFindHenkilosWithoutKayttajatiedot() throws Exception {
        var response = mvc.perform(post("/internal/virkailijahaku")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
    {"nameQuery":"Olli"}""")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<HenkilohakuResultDto> result = objectMapper.readValue(response, new TypeReference<List<HenkilohakuResultDto>>(){});
        assertThat(result).hasSize(0);
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void virkailijahakuForOphUserFindsUsersWithoutOrganisation() throws Exception {
        var response = mvc.perform(post("/internal/virkailijahaku")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
    {"nameQuery":"pasi"}""")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<HenkilohakuResultDto> result = objectMapper.readValue(response, new TypeReference<List<HenkilohakuResultDto>>(){});
        assertThat(result)
                .extracting("kayttajatunnus")
                .containsExactlyInAnyOrder("pasi");
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.23462357366", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void virkailijahakuForNonOphUserDoesNotFindUsersWithPassiveOrganisation() throws Exception {
        var response = mvc.perform(post("/internal/virkailijahaku")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
    {"nameQuery":"pasi"}""")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<HenkilohakuResultDto> result = objectMapper.readValue(response, new TypeReference<List<HenkilohakuResultDto>>(){});
        assertThat(result).hasSize(0);
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void virkailijahakuFiltersVirkailijasByNameWithNameQuery() throws Exception {
        var response = mvc.perform(post("/internal/virkailijahaku")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
    {"nameQuery":"Virkailija"}""")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<HenkilohakuResultDto> result = objectMapper.readValue(response, new TypeReference<List<HenkilohakuResultDto>>(){});
        assertThat(result)
                .extracting("kayttajatunnus")
                .containsExactlyInAnyOrder("ville");
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void virkailijahakuFiltersVirkailijasByOidWithNameQuery() throws Exception {
        var response = mvc.perform(post("/internal/virkailijahaku")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
    {"nameQuery":"1.2.246.562.24.12342342565"}""")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<HenkilohakuResultDto> result = objectMapper.readValue(response, new TypeReference<List<HenkilohakuResultDto>>(){});
        assertThat(result)
                .extracting("kayttajatunnus")
                .containsExactlyInAnyOrder("pasi");
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void virkailijahakuFiltersVirkailijasByUsernameWithNameQuery() throws Exception {
        var response = mvc.perform(post("/internal/virkailijahaku")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
    {"nameQuery":"opa"}""")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<HenkilohakuResultDto> result = objectMapper.readValue(response, new TypeReference<List<HenkilohakuResultDto>>(){});
        assertThat(result)
                .extracting("kayttajatunnus")
                .containsExactlyInAnyOrder("opa");
        assertThat(result.get(0).getOrganisaatioNimiList())
                .extracting("identifier")
                .containsExactlyInAnyOrder("1.2.246.562.10.00000000001", "1.2.246.562.10.71948887212");
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void virkailijahakuFiltersVirkailijasByOrganisation() throws Exception {
        var response = mvc.perform(post("/internal/virkailijahaku")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
    {"organisaatioOids":["1.2.246.562.10.71948887212"]}""")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<HenkilohakuResultDto> result = objectMapper.readValue(response, new TypeReference<List<HenkilohakuResultDto>>(){});
        assertThat(result)
                .extracting("kayttajatunnus")
                .containsExactlyInAnyOrder("opa", "ville");
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void jarjestelmatunnushakuFiltersVirkailijasByKayttoikeusryhmaId() throws Exception {
        var response = mvc.perform(post("/internal/virkailijahaku")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
    {"kayttooikeusryhmaId":333}""")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<HenkilohakuResultDto> result = objectMapper.readValue(response, new TypeReference<List<HenkilohakuResultDto>>(){});
        assertThat(result)
                .extracting("kayttajatunnus")
                .containsExactlyInAnyOrder("opa", "ville");
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA")
    public void jarjestelmatunnushakuFiltersJarjestelmatunnusByUsername() throws Exception {
        var response = mvc.perform(post("/internal/jarjestelmatunnushaku")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
    {"nameQuery":"paten"}""")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<HenkilohakuResultDto> result = objectMapper.readValue(response, new TypeReference<List<HenkilohakuResultDto>>(){});
        assertThat(result)
                .extracting("kayttajatunnus")
                .containsExactlyInAnyOrder("patenpalvelu");
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA")
    public void jarjestelmatunnushakuFiltersJarjestelmatunnusByName() throws Exception {
        var response = mvc.perform(post("/internal/jarjestelmatunnushaku")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
    {"nameQuery":"krypt"}""")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<HenkilohakuResultDto> result = objectMapper.readValue(response, new TypeReference<List<HenkilohakuResultDto>>(){});
        assertThat(result)
                .extracting("kayttajatunnus")
                .containsExactlyInAnyOrder("patenpalvelu");
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA")
    public void jarjestelmatunnushakuFiltersJarjestelmatunnusByOrganisation() throws Exception {
        var response = mvc.perform(post("/internal/jarjestelmatunnushaku")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
    {"organisaatioOids":["1.2.246.562.10.00000000001"]}""")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<HenkilohakuResultDto> result = objectMapper.readValue(response, new TypeReference<List<HenkilohakuResultDto>>(){});
        assertThat(result)
                .extracting("kayttajatunnus")
                .containsExactlyInAnyOrder("patenpalvelu");
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA")
    public void jarjestelmatunnushakuFiltersJarjestelmatunnusByKayttooikeusryhmaId() throws Exception {
        var response = mvc.perform(post("/internal/jarjestelmatunnushaku")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
    {"kayttooikeusryhmaId":444}""")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<HenkilohakuResultDto> result = objectMapper.readValue(response, new TypeReference<List<HenkilohakuResultDto>>(){});
        assertThat(result)
                .extracting("kayttajatunnus")
                .containsExactlyInAnyOrder("patenpalvelu");
    }
}
