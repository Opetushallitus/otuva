package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.KutsuListDto;
import fi.vm.sade.kayttooikeus.dto.KutsuOrganisaatioListDto;
import fi.vm.sade.kayttooikeus.dto.TextGroupMapDto;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class KutsuControllerTest extends AbstractControllerTest {
    @MockBean
    private KutsuService kutsuService;

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_CRUD")
    public void listAvoinKutsusTest() throws Exception {
        given(this.kutsuService.listAvoinKutsus(any()))
                .willReturn(singletonList(KutsuListDto.builder()
                        .id(1L).aikaleima(ZonedDateTime.of(2016,1,1, 0, 0, 0, 0, ZoneId.systemDefault()))
                        .sahkoposti("posti@example.com")
                        .organisaatiot(singletonList(
                            KutsuOrganisaatioListDto.builder()
                                .id(2L).nimi(new TextGroupMapDto(3L).put("FI", "Oikeus"))
                                .oid("OID")
                            .build()
                        ))
                    .build()));
        this.mvc.perform(get("/kutsu").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResource("classpath:kutsu/simpleKutsuListaus.json")));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_CRUD")
    public void createTest() throws Exception {
        given(kutsuService.createKutsu(any())).willReturn(1L);

        mvc.perform(post("/kutsu").contentType(MediaType.APPLICATION_JSON).content(jsonResource("classpath:kutsu/simpleKutsuLuonti.json")))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", endsWith("/kutsu/1")));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_CRUD")
    public void deleteTest() throws Exception {
        this.mvc.perform(delete("/kutsu/1").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }
}
