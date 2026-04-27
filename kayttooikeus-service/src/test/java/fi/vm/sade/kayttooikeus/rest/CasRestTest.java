package fi.vm.sade.kayttooikeus.rest;

import fi.vm.sade.kayttooikeus.DatabaseService;
import fi.vm.sade.kayttooikeus.config.security.TunnistusSecurityConfig;
import fi.vm.sade.kayttooikeus.dto.KutsunTila;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;

import org.apereo.cas.client.authentication.AttributePrincipal;
import org.apereo.cas.client.validation.Assertion;
import org.apereo.cas.client.validation.TicketValidationException;
import org.apereo.cas.client.validation.TicketValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static fi.vm.sade.kayttooikeus.config.security.casoppija.SuomiFiAuthenticationProcessingFilter.ETUNIMET_ATTRIBUTE;
import static fi.vm.sade.kayttooikeus.config.security.casoppija.SuomiFiAuthenticationProcessingFilter.HETU_ATTRIBUTE;
import static fi.vm.sade.kayttooikeus.config.security.casoppija.SuomiFiAuthenticationProcessingFilter.SUKUNIMI_ATTRIBUTE;
import static fi.vm.sade.kayttooikeus.controller.KutsuPopulator.kutsu;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CasRestTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DatabaseService databaseService;

    @MockitoBean
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @MockitoBean(name = TunnistusSecurityConfig.OPPIJA_TICKET_VALIDATOR_QUALIFIER)
    private TicketValidator oppijaTicketValidator;

    @BeforeEach
    public void setup() throws TicketValidationException {
        Assertion assertion = mock(Assertion.class);
        AttributePrincipal principal = mock(AttributePrincipal.class);
        Map<String,Object> attributes = new HashMap<>();
        attributes.put(HETU_ATTRIBUTE, "hetu123");
        attributes.put(SUKUNIMI_ATTRIBUTE, "Testi");
        attributes.put(ETUNIMET_ATTRIBUTE, "Testi-Petteri");
        when(oppijaTicketValidator.validate(anyString(), anyString()))
                .thenReturn(assertion);
        when(assertion.isValid()).thenReturn(true);
        when(assertion.getPrincipal()).thenReturn(principal);
        when(principal.getAttributes()).thenReturn(attributes);
    }

    @AfterEach
    public void cleanup() {
        databaseService.truncate();
    }

    @Test
    public void tunnistusKutsuToken() throws Exception {
        databaseService.populate(kutsu("etu", "suku", "sahkoposti@example.com")
                .tila(KutsunTila.AVOIN)
                .salaisuus("kutsuToken123")
                .aikaleima(LocalDateTime.now()));
        mockMvc.perform(get("/cas/tunnistus")
                .param("kutsuToken", "kutsuToken123")
                .param("locale", "kielisyys123")
                .param("ticket", "password"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        containsString(urlEncode("/henkilo-ui/kayttaja/rekisteroidy?temporaryKutsuToken="))));
    }

    @Test
    public void tunnistusKutsuTokenEiLoydy() throws Exception {
        mockMvc.perform(get("/cas/tunnistus")
                .param("kutsuToken", "kutsuToken123")
                .param("locale", "fi")
                .param("ticket", "password"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        endsWith(urlEncode("/henkilo-ui/kayttaja/kutsu/vanhentunut/fi"))));
    }

    private String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
