package fi.vm.sade.kayttooikeus.controller;


import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.TaskExecutorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
public class KayttoOikeusControllerTest extends AbstractControllerTest {
    @MockitoBean
    private KayttoOikeusService kayttoOikeusService;
    @MockitoBean
    private TaskExecutorService taskExecutorService;

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_READ")
    public void listKayttoOikeusByPalveluTest() throws Exception {
        given(this.kayttoOikeusService.listKayttoOikeusByPalvelu("HENKILOHALLINTA"))
            .willReturn(singletonList(PalveluKayttoOikeusDto.builder()
                    .rooli("ROLE").oikeusLangs(new TextGroupListDto(1L).put("FI", "Nimi")
                        .put("EN", "Name"))
                    .build()));
        this.mvc.perform(get("/kayttooikeus/HENKILOHALLINTA").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResource("classpath:kayttooikeus/palveluOikeusList.json")));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_READ")
    public void listKayttoOikeusCurrentUserTest() throws Exception {
        given(this.kayttoOikeusService.listMyonnettyKayttoOikeusHistoriaForCurrentUser())
                .willReturn(singletonList(KayttoOikeusHistoriaDto.builder()
                        .aikaleima(LocalDate.of(2015, 1, 1).atStartOfDay())
                        .kasittelija("kasittelija")
                        .kayttoOikeusRyhmaId(5L)
                        .kayttoOikeusId(1L)
                        .kuvaus(new TextGroupListDto(2L).put("FI", "nimi"))
                        .rooli("ROOLI")
                        .kayttoOikeusKuvaus(new TextGroupListDto(3L).put("FI", "Oikeus"))
                        .palvelu("PALVELU")
                        .palveluKuvaus(new TextGroupListDto(4L).put("FI", "Palvelu"))
                        .organisaatioOid("orgOid")
                        .tehtavanimike("nimike")
                        .tila(KayttoOikeudenTila.HYLATTY)
                        .tyyppi(KayttoOikeusTyyppi.KOOSTEROOLI)
                        .voimassaAlkuPvm(LocalDate.of(2015, 1, 1))
                        .voimassaLoppuPvm(LocalDate.of(2015, 12, 31))
                .build()));
        this.mvc.perform(get("/kayttooikeus/kayttaja/current").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResource("classpath:kayttooikeus/currentUserHistoria.json")));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_READ")
    public void listKayttoOikeusForUserTest() throws Exception {
        given(this.kayttoOikeusService.listMyonnettyKayttoOikeusForUser(any(), any(), any()))
                .willReturn(singletonList(KayttooikeusPerustiedotDto.builder()
                        .kayttajaTyyppi(KayttajaTyyppi.OPPIJA)
                        .oidHenkilo("string")
                        .username("string")
                        .organisaatiot(singleton(KayttooikeusPerustiedotDto.KayttooikeusOrganisaatiotDto.builder()
                                .organisaatioOid("string")
                                .kayttooikeudet(singleton(KayttooikeusPerustiedotDto.KayttooikeusOrganisaatiotDto.KayttooikeusOikeudetDto.builder()
                                        .oikeus("string")
                                        .palvelu("string")
                                        .build()))
                                .build()))
                        .build()));
        this.mvc.perform(get("/kayttooikeus/kayttaja?hetu=123456-7890").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResource("classpath:kayttooikeus/kayttooikeusForUser.json")));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_READ")
    public void listKayttoOikeusForUserAccessLogMaskingTest() throws Exception {
        given(this.kayttoOikeusService.listMyonnettyKayttoOikeusForUser(any(), any(), any()))
                .willReturn(Collections.EMPTY_LIST);
        this.mvc.perform(get("/kayttooikeus/kayttaja?hetu=123456-7890").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA")
    public void sendExpirationRemindersTest() throws Exception {
        given(this.taskExecutorService.sendExpirationReminders(any(Period.class))).willReturn(1);
        this.mvc.perform(post("/kayttooikeus/expirationReminders")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .param("year", "2015").param("month", "1").param("day", "1"))
            .andExpect(status().isOk()).andExpect(content().string(is("1")));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA")
    public void getHaettuKayttooikeusRyhmasByOidTest() throws Exception{
        Map<String, List<Integer>> kayttooikeusRyhmasByOrganisaatio
                = Collections.singletonMap("1.0.0.1.0", Collections.singletonList(12000));
        String expectedContent = "{  \"1.0.0.1.0\": [" +
                "    12000" +
                "  ]}";
        given(this.kayttoOikeusService.findKayttooikeusryhmatAndOrganisaatioByHenkiloOid(anyString()))
                .willReturn(kayttooikeusRyhmasByOrganisaatio);
        this.mvc.perform(get("/kayttooikeusryhma/ryhmasByOrganisaatio/1.0.0.1.0").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().json(expectedContent));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA")
    public void getHaettuKayttooikeusRyhmasByOidInvalidDataTest() throws Exception{
        given(this.kayttoOikeusService.findKayttooikeusryhmatAndOrganisaatioByHenkiloOid(anyString()))
                .willThrow(new NullPointerException("null_ryhma_id"));
        this.mvc.perform(get("/kayttooikeusryhma/ryhmasByOrganisaatio/1.0.0.1.0").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
