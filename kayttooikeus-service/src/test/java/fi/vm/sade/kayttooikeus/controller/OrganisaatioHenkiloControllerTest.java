package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
public class OrganisaatioHenkiloControllerTest extends AbstractControllerTest {
    @MockitoBean
    private OrganisaatioHenkiloService service;
    @MockitoBean
    private OrganisaatioClient organisaatioClient;

    @Test
    @WithMockUser(roles = "APP_KAYTTOOIKEUS_CRUD")
    public void passivoiHenkiloOrganisationTest() throws Exception {
        this.mvc.perform(delete("/organisaatiohenkilo/henkiloOid/organisaatioOid").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
