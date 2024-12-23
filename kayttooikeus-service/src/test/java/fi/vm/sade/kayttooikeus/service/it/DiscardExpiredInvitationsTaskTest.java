package fi.vm.sade.kayttooikeus.service.it;

import fi.vm.sade.kayttooikeus.client.viestinvalitys.*;
import fi.vm.sade.kayttooikeus.config.ApplicationTest;
import fi.vm.sade.kayttooikeus.config.scheduling.DiscardExpiredInvitationsTask;
import fi.vm.sade.kayttooikeus.dto.Asiointikieli;
import fi.vm.sade.kayttooikeus.dto.KayttajaTyyppi;
import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Period;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RunWith(SpringRunner.class)
@ApplicationTest
public class DiscardExpiredInvitationsTaskTest {
    @Autowired
    private DiscardExpiredInvitationsTask discardExpiredInvitationsTask;

    @Autowired
    private KutsuService kutsuService;

    @MockBean
    private HenkiloDataRepository henkiloDataRepository;

    @MockBean
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @MockBean
    private ViestinvalitysClient viestinvalitysClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String INVALID_EMAIL = "invalid@email";
    private final Period period = Period.ZERO;

    @Before
    public void before() {
        String kutsujaHenkiloOid = "1.2.3.4.5";
        var henkilo = Henkilo.builder()
                .oidHenkilo(kutsujaHenkiloOid)
                .kayttajaTyyppi(KayttajaTyyppi.VIRKAILIJA)
                .build();
        Mockito.when(henkiloDataRepository.findByOidHenkilo(kutsujaHenkiloOid)).thenReturn(Optional.of(henkilo));

        var kutsuja = HenkiloDto.builder()
                .oidHenkilo(kutsujaHenkiloOid)
                .hetu("010175-9999")
                .yksiloityVTJ(true)
                .build();
        Mockito.when(oppijanumerorekisteriClient.getHenkiloByOid(kutsujaHenkiloOid)).thenReturn(kutsuja);

        var lahetysTunniste = UUID.randomUUID().toString();
        var response = new LuoLahetysSuccessResponse();
        response.setLahetysTunniste(lahetysTunniste);
        Mockito.when(viestinvalitysClient.luoLahetys(Mockito.any(Lahetys.class))).thenReturn(response);

        deleteKutsut();
        deleteQueuedEmails();
    }

    @After
    public void tearDown() {
        deleteKutsut();
        deleteQueuedEmails();
    }

    private ArgumentMatcher<Viesti> containsAnInvalidEmailAddress() {
        return (viesti) -> viesti
                .getVastaanottajat()
                .stream()
                .anyMatch(vastaanottaja -> INVALID_EMAIL.equals(vastaanottaja.getSahkopostiOsoite()));
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    @Description("Send an email for each expired kutsu and delete kutsu")
    public void sendAnEmailForEachExpiredKutsuAndDeleteKutsu() {
        var kutsuCount = 10;

        for (int i = 0; i < kutsuCount; i++) {
            createKutsuWithValidEmail();
        }

        Assertions.assertEquals(0, getQueuedEmailMarkedAsSentCount(), "No mail should have be sent");
        Assertions.assertEquals(kutsuCount, kutsuService.findExpired(period).size(), kutsuCount + " kutsu should be expired");

        discardExpiredInvitationsTask.expire("expire", kutsuService, period);

        Assertions.assertEquals(kutsuCount, getQueuedEmailMarkedAsSentCount(), kutsuCount + " mails should have been sent");
        Assertions.assertEquals(0, kutsuService.findExpired(period).size(), "No kutsu should be expired");
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5")
    @Description("Send email for each expired kutsu and delete kutsu even if some kutsu has an invalid address")
    public void sendEmailForEachExpiredKutsuAndDeleteKutsuEvenIfSomeKutsuHasAnInvalidAddress() {
        var kutsuCount = 11;

        for (int i = 0; i < kutsuCount - 1; i++) {
            createKutsuWithValidEmail();
        }
        createKutsuWithInvalidEmail();

        Assertions.assertEquals(0, getQueuedEmailMarkedAsSentCount(), "No email should have be marked as sent");
        Assertions.assertEquals(kutsuCount, kutsuService.findExpired(period).size(), kutsuCount + " kutsu should be expired");
        Mockito.when(viestinvalitysClient.luoViesti(Mockito.argThat(containsAnInvalidEmailAddress())))
                .thenThrow(new BadRequestException(new ApiResponse(400, "")));

        discardExpiredInvitationsTask.expire("name", kutsuService, period);

        Assertions.assertEquals((kutsuCount - 1), getQueuedEmailMarkedAsSentCount(), (kutsuCount - 1) + " emails should have been marked as sent");
        Assertions.assertEquals(1, kutsuService.findExpired(period).size(), "One kutsu should be expired");
    }

    private void createKutsuWithValidEmail() {
        createKutsu("valid@email.com");
    }

    private void createKutsuWithInvalidEmail() {
        createKutsu(INVALID_EMAIL);
    }

    private void createKutsu(String email) {
        var kutsu = KutsuCreateDto.builder()
                .etunimi("etunimi")
                .sukunimi("sukunimi")
                .sahkoposti(email)
                .asiointikieli(Asiointikieli.fi)
                .organisaatiot(Set.of())
                .build();

        kutsuService.createKutsu(kutsu);
    }

    private void deleteQueuedEmails() {
        jdbcTemplate.update("delete from queuedemail");
    }

    private void deleteKutsut() {
        jdbcTemplate.update("delete from kutsu");
    }

    private Integer getQueuedEmailMarkedAsSentCount() {
        String sql = """
            select count(*)
            from queuedemail
            where queuedemailstatus_id = 'SENT'
            and subject = 'Kutsu Opintopolun virkailijapalveluun poistettiin automaattisesti'
            """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

}
