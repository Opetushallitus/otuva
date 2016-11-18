package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloTyyppi;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloWithOrganisaatioDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloWithOrganisaatioDto.OrganisaatioDto;
import fi.vm.sade.kayttooikeus.dto.TextGroupMapDto;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class HenkiloControllerTest extends AbstractControllerTest {
    @MockBean
    private OrganisaatioHenkiloService service;
    
    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void listOrganisaatioPerustiedotForCurrentUserTest() throws Exception {
        given(this.service.listOrganisaatioPerustiedotForCurrentUser()).willReturn(new ArrayList<>());
        this.mvc.perform(get("/henkilo/current/organisaatio").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void listOrganisatioHenkilosTest() throws Exception {
        given(this.service.listOrganisaatioHenkilos("1.2.3.4.5", "fi")).willReturn(singletonList(
                OrganisaatioHenkiloWithOrganisaatioDto.organisaatioBuilder().id(1L)
                        .voimassaAlkuPvm(new LocalDate(2016,1,1))
                        .voimassaLoppuPvm(new LocalDate(2016,12,31))
                        .tyyppi(OrganisaatioHenkiloTyyppi.HAKIJA)
                        .passivoitu(false)
                        .tehtavanimike("Devaaja")
                        .organisaatio(OrganisaatioDto.builder()
                                .oid("1.2.3.4.7")
                                .nimi(new TextGroupMapDto().put("fi", "Suomeksi")
                                        .put("sv", "Ruotsiksi"))
                                .tyypit(singletonList("OPPILAITOS"))
                        .build())
                .build()
        ));
        this.mvc.perform(get("/henkilo/1.2.3.4.5/organisaatio").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andExpect(content()
                .json(jsonResource("classpath:henkilo/henkiloOrganisaatios.json")));
    }
}
