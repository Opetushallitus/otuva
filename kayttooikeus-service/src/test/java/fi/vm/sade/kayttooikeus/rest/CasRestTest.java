package fi.vm.sade.kayttooikeus.rest;

import fi.vm.sade.kayttooikeus.DatabaseService;
import fi.vm.sade.kayttooikeus.config.security.TunnistusSecurityConfig;
import fi.vm.sade.kayttooikeus.dto.KutsunTila;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;

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
import java.util.Optional;

import static fi.vm.sade.kayttooikeus.config.security.casoppija.SuomiFiAuthenticationProcessingFilter.ETUNIMET_ATTRIBUTE;
import static fi.vm.sade.kayttooikeus.config.security.casoppija.SuomiFiAuthenticationProcessingFilter.HETU_ATTRIBUTE;
import static fi.vm.sade.kayttooikeus.config.security.casoppija.SuomiFiAuthenticationProcessingFilter.SUKUNIMI_ATTRIBUTE;
import static fi.vm.sade.kayttooikeus.controller.KutsuPopulator.kutsu;
import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.TunnistusTokenPopulator.tunnistusToken;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
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
                .param("locale", "kielisyys123")
                .param("ticket", "password"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        endsWith(urlEncode("/henkilo-ui/kayttaja/vahvatunnistusinfo/virhe/kielisyys123/vanhakutsu"))));
    }

    @Test
    public void tunnistusLoginToken() throws Exception {
        databaseService.populate(tunnistusToken(henkilo("henkilo123").withUsername("kayttaja123"))
                .loginToken("loginToken123")
                .aikaleima(LocalDateTime.now()));
        when(oppijanumerorekisteriClient.getHenkiloByOid(any())).thenReturn(HenkiloDto.builder()
                .oidHenkilo("henkilo123")
                .hetu("hetu123")
                .build());

        mockMvc.perform(get("/cas/tunnistus")
                .param("loginToken", "loginToken123")
                .param("locale", "kielisyys123")
                .param("ticket", "password"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        containsString(urlEncode("/henkilo-ui/kayttaja/uudelleenrekisterointi/kielisyys123/loginToken123/"))));
    }

    @Test
    public void tunnistusLoginTokenEiLoydy() throws Exception {
        mockMvc.perform(get("/cas/tunnistus")
                .param("loginToken", "loginToken123")
                .param("locale", "kielisyys123")
                .param("ticket", "password"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        endsWith(urlEncode("/henkilo-ui/kayttaja/vahvatunnistusinfo/virhe/kielisyys123/vanha"))));
    }

    @Test
    public void tunnistusLoginTokenOppijanumerorekisteriEiToimi() throws Exception {
        databaseService.populate(tunnistusToken(henkilo("henkilo123"))
                .loginToken("loginToken123")
                .aikaleima(LocalDateTime.now()));
        when(oppijanumerorekisteriClient.getHenkiloByOid(any())).thenThrow(new RuntimeException("oppijanumerorekisteri ei toimi"));

        mockMvc.perform(get("/cas/tunnistus")
                .param("loginToken", "loginToken123")
                .param("locale", "kielisyys123")
                .param("ticket", "password"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        endsWith(urlEncode("/henkilo-ui/kayttaja/vahvatunnistusinfo/virhe/kielisyys123/loginToken123"))));
    }

    @Test
    public void tunnistusVahvaTunnistautuminen() throws Exception {
        databaseService.populate(organisaatioHenkilo(henkilo("henkilo123")
                .withUsername("kayttaja123"), "organisaatio123"));
        when(oppijanumerorekisteriClient.getHenkiloByHetu(any())).thenReturn(Optional.of(HenkiloDto.builder()
                .oidHenkilo("henkilo123")
                .build()));

        mockMvc.perform(get("/cas/tunnistus")
                .param("locale", "kielisyys123")
                .param("ticket", "password"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        containsString(urlEncode("/henkilo-ui/kayttaja/uudelleenrekisterointi/kielisyys123/"))));
    }

    @Test
    public void tunnistusVahvaTunnistautuminenHetuEiLoydy() throws Exception {
        when(oppijanumerorekisteriClient.getHenkiloByHetu(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/cas/tunnistus")
                .param("locale", "kielisyys123")
                .param("ticket", "password"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        endsWith(urlEncode("/henkilo-ui/kayttaja/vahvatunnistusinfo/virhe/kielisyys123/eiloydy"))));
    }

    private String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
