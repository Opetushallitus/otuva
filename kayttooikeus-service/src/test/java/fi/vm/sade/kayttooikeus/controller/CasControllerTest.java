package fi.vm.sade.kayttooikeus.controller;


import fi.vm.sade.kayttooikeus.dto.KayttajaTyyppi;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Identification;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.service.IdentificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
public class CasControllerTest extends AbstractControllerTest {

    @MockitoBean
    private IdentificationService identificationService;

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA")
    public void generateAuthTokenForHenkiloTest() throws Exception {
        given(this.identificationService.generateAuthTokenForHenkilo("1.2.3.4.9", "somekey", "someidentifier"))
                .willReturn("myrandomtoken");

        this.mvc.perform(get("/cas/auth/oid/1.2.3.4.9")
                .param("idpid", "someidentifier"))
                .andExpect(status().is5xxServerError());

        this.mvc.perform(get("/cas/auth/oid/1.2.3.4.9")
                .param("idpkey", "somekey")
                .param("idpid", "someidentifier"))
                .andExpect(status().isOk())
                .andExpect(content().string("\"myrandomtoken\""));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA")
    public void getHenkiloOidByIdPAndIdentifierTest() throws Exception {
        given(identificationService.getHenkiloOidByIdpAndIdentifier("somekey", "someidentifier"))
                .willReturn("token");
        this.mvc.perform(get("/cas/auth/idp/somekey").param("idpid", "someidentifier")
                .param("idpid", "someidentifier"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA")
    public void getIdentityByAuthTokenTest() throws Exception {
        var identification = Identification.builder()
                .henkilo(Henkilo.builder()
                        .oidHenkilo("1.2.3.4.5")
                        .kayttajatiedot(Kayttajatiedot.builder()
                                .username("teemuuser")
                                .mfaProvider(null)
                                .build())
                        .kayttajaTyyppi(KayttajaTyyppi.VIRKAILIJA)
                        .build())
                .idpEntityId(null)
                .identifier("mytoken")
                .build();

        given(identificationService.findByTokenAndInvalidateToken("mytoken")).willReturn(identification);

        this.mvc.perform(get("/cas/auth/token/mytoken"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                                "username": "teemuuser",
                                "oidHenkilo": "1.2.3.4.5",
                                "kayttajaTyyppi": "VIRKAILIJA"
                        }"""));
    }

}
