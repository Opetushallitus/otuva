package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.config.ApplicationTest;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static fi.vm.sade.kayttooikeus.model.HenkiloTyyppi.VIRKAILIJA;
import static java.util.Collections.singletonList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ApplicationTest
@AutoConfigureMockMvc
public class OrganisaatioHenkiloControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private OrganisaatioHenkiloService service;

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void listOrganisaatioPerustiedotForCurrentUserTest() throws Exception {
        given(this.service.listOrganisaatioPerustiedotForCurrentUser()).willReturn(new ArrayList<>());
        this.mvc.perform(get("/organisaatiohenkilo/current/organisaatio").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }
    
    @Test
    public void listOrganisaatioPerustiedotForCurrentUserIsSecuredTest() throws Exception {
        this.mvc.perform(get("/organisaatiohenkilo/current/organisaatio").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is3xxRedirection()); // redirect to CAS login
    }
    
    @Test
    @WithMockUser(roles = "APP_HENKILONHALLINTA_CRUD")
    public void listPossibleHenkiloTypesByCurrentHenkiloTest() throws Exception {
        given(this.service.listPossibleHenkiloTypesAccessibleForCurrentUser()).willReturn(singletonList(VIRKAILIJA));
        this.mvc.perform(get("/organisaatiohenkilo/current/availablehenkilotype").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andExpect(content().json("['VIRKAILIJA']"));
    }

    @Test
    public void listPossibleHenkiloTypesByCurrentHenkiloIsSecuredTest() throws Exception {
        this.mvc.perform(get("/organisaatiohenkilo/current/availablehenkilotype").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is3xxRedirection()); // redirect to CAS login
    }
}
