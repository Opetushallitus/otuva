package fi.vm.sade.kayttooikeus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import fi.vm.sade.kayttooikeus.dto.GrantKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.dto.KayttooikeusAnomusDto;
import fi.vm.sade.kayttooikeus.dto.UpdateHaettuKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.service.KayttooikeusAnomusService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class AnomusControllerTest extends AbstractControllerTest {
    @MockBean
    private KayttooikeusAnomusService kayttooikeusAnomusService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void getActiveAnomuksetByHenkilo() throws Exception {
        given(this.kayttooikeusAnomusService.getAllActiveAnomusByHenkiloOid(anyString(), anyBoolean()))
                .willReturn(new ArrayList<>());
        this.mvc.perform(get("/kayttooikeusanomus/1.2.3.4.5").param("activeOnly", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void getActiveAnomuksetByHenkiloNotFound() throws Exception {
        given(this.kayttooikeusAnomusService.getAllActiveAnomusByHenkiloOid(anyString(), anyBoolean()))
                .willThrow(new NotFoundException("message"));
        this.mvc.perform(get("/kayttooikeusanomus/1.2.3.4.5").param("activeOnly", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void updateHaettuKayttooikeusryhma() throws Exception {
        UpdateHaettuKayttooikeusryhmaDto haettuKayttooikeusryhmaDto = new UpdateHaettuKayttooikeusryhmaDto(1L,
                KayttoOikeudenTila.MYONNETTY.toString(), DateTime.now().toLocalDate(), DateTime.now().plusYears(1).toLocalDate());
        this.mvc.perform(put("/kayttooikeusanomus")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(haettuKayttooikeusryhmaDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void grantMyonnettyKayttooikeusryhmaForHenkilo() throws Exception {
        GrantKayttooikeusryhmaDto grantKayttooikeusryhmaDto = new GrantKayttooikeusryhmaDto(1L,
                DateTime.now().toLocalDate(), DateTime.now().plusYears(1).toLocalDate());
        this.mvc.perform(put("/kayttooikeusanomus/1.2.3.4.5/1.2.0.0.1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(Lists.newArrayList(grantKayttooikeusryhmaDto))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void createKayttooikeusAnomus() throws Exception {
        ArrayList<Long> oids = new ArrayList<Long>();
        oids.add(1L);
        KayttooikeusAnomusDto kayttooikeusAnomusDto = new KayttooikeusAnomusDto("1.2.3.4.5",
                "Tehtävänimike", "email@domain.com", oids, "perustelut");
        this.mvc.perform(post("/kayttooikeusanomus/1.2.3.4.5")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(kayttooikeusAnomusDto)))
                .andExpect(status().isOk());
    }
}