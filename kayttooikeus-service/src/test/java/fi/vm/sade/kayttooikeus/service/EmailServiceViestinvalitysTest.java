package fi.vm.sade.kayttooikeus.service;

import com.google.common.collect.Sets;
import fi.vm.sade.kayttooikeus.dto.TextGroupDto;
import fi.vm.sade.kayttooikeus.dto.UpdateHaettuKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaRepository;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.service.QueueingEmailService.QueuedEmail;
import fi.vm.sade.kayttooikeus.service.external.*;
import fi.vm.sade.kayttooikeus.service.impl.EmailServiceViestinvalitysImpl;
import fi.vm.sade.kayttooikeus.util.CreateUtil;
import fi.vm.sade.kayttooikeus.util.YhteystietoUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static java.util.Collections.singleton;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class EmailServiceViestinvalitysTest extends AbstractServiceTest {

    private static final String HENKILO_OID = "1.2.3.4.5";
    private static final String WORK_EMAIL = "testi@example.com";

    private static final String TEST_LANG = "fi";
    private static final String TEST_EMAIL = "arpa@kuutio.fi";
    private static final String TEST_FIRST_NAME = "arpa";
    private static final String TEST_LAST_NAME = "kuutio";

    @MockBean
    private QueueingEmailService queueingEmailService;

    @MockBean
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @MockBean
    private KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository;

    @MockBean
    private OrganisaatioClient organisaatioClient;

    @Autowired
    private EmailServiceViestinvalitysImpl emailService;

    @Test
    @WithMockUser(username = "user1")
    public void sendExpirationReminderTest() {
        given(oppijanumerorekisteriClient.getHenkiloByOid(HENKILO_OID)).willReturn(getHenkilo());
        given(queueingEmailService.queueEmail(any())).willReturn("id");

        emailService.sendExpirationReminder(HENKILO_OID, Collections.singletonList(
                ExpiringKayttoOikeusDto.builder()
                        .henkiloOid(HENKILO_OID)
                        .myonnettyTapahtumaId(1L)
                        .ryhmaName("RYHMA")
                        .ryhmaDescription(new TextGroupDto(2L).put("FI", "Kuvaus")
                                .put("EN", "Desc"))
                        .voimassaLoppuPvm(LocalDate.of(2021, 10, 8))
                        .build())
        );

        ArgumentCaptor<QueuedEmail> emailCaptor = ArgumentCaptor.forClass(QueuedEmail.class);
        verify(queueingEmailService, times(1)).queueEmail(emailCaptor.capture());
        QueuedEmail email = emailCaptor.getValue();
        assertThat(email.getRecipients().size()).isEqualTo(1);
        assertThat(email.getRecipients().get(0)).isEqualTo(WORK_EMAIL);
        assertThat(email.getBody()).isEqualToIgnoringWhitespace("""
<!doctype html>
<html lang="fi">
<head>
    <meta charset="utf-8">
    <title>Virkailijan opintopolku: käyttöoikeuksia vanhenemassa</title>
    <style>
        body {
            background: #F6F4F0;
        }
        .box {
            background: #FFFFFF;
            padding: 1em 2em;
            margin: 2em 4em;
        }
    </style>
</head>
<body>
    <div class="box">
        <h3>Virkailijan opintopolku: käyttöoikeuksia vanhenemassa</h3>
        <p>Hei Heiki, </p>
        <p>
            Käyttöoikeutesi seuraaviin Opintopolussa oleviin palveluihin ovat vanhenemassa (suluissa vanhenemispäivä): Kuvaus (8.10.2021)
        </p>
        <p>
            Kirjaudu Opintopolkuun ja hae jatkoaikaa omista tiedoistasi (sivun yläkulmassa oma nimi). Voit jatkaa palveluun tästä: <a href="http://testilinkki.fi/omattiedot">http://testilinkki.fi/omattiedot</a>
        </p>
    </div>
    <div class="box" style="text-align: right;">
        <img src="http://www.oph.fi/instancedata/prime_product_julkaisu/oph/pics/opetushallitus2.gif" alt="Opetushallitus"/>
    </div>
</body>
</html>""");
    }

    private HenkiloDto getHenkilo() {
        HenkiloDto henkiloDto = new HenkiloDto();
        henkiloDto.setEtunimet("Heiki");
        henkiloDto.setYhteystiedotRyhma(singleton(YhteystiedotRyhmaDto
                .builder()
                .ryhmaKuvaus(YhteystietoUtil.TYOOSOITE)
                .yhteystieto(YhteystietoDto.builder()
                        .yhteystietoTyyppi(YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI)
                        .yhteystietoArvo(WORK_EMAIL)
                        .build())
                .build()));
        return henkiloDto;
    }

    @Test
    @WithMockUser(username = "user1")
    public void sendExpirationReminderNoWorkEmailTest() {
        given(oppijanumerorekisteriClient.getHenkiloByOid(HENKILO_OID)).willReturn(new HenkiloDto());

        emailService.sendExpirationReminder(HENKILO_OID, Collections.EMPTY_LIST);

        verify(queueingEmailService, never()).queueEmail(any());
    }

    @Test
    @WithMockUser(username = "user1")
    public void NewRequisitionNotificationTest() {
        given(oppijanumerorekisteriClient.getHenkiloByOid(HENKILO_OID)).willReturn(getHenkilo());
        given(queueingEmailService.queueEmail(any())).willReturn("id");

        emailService.sendNewRequisitionNotificationEmails(Collections.singleton(HENKILO_OID));

        ArgumentCaptor<QueuedEmail> emailCaptor = ArgumentCaptor.forClass(QueuedEmail.class);
        verify(queueingEmailService, times(1)).queueEmail(emailCaptor.capture());
        QueuedEmail email = emailCaptor.getValue();
        assertThat(email.getRecipients().size()).isEqualTo(1);
        assertThat(email.getRecipients().get(0)).isEqualTo("testi@example.com");
        assertThat(email.getBody()).isEqualToIgnoringWhitespace("""
<!doctype html>
<html lang="fi">
<head>
    <meta charset="utf-8">
    <title>Virkailijan opintopolku: käyttöoikeusanomuksia saapunut</title>
    <style>
        body {
            background: #F6F4F0;
        }
        .box {
            background: #FFFFFF;
            padding: 1em 2em;
            margin: 2em 4em;
        }
    </style>
</head>
<body>
    <div class="box">
        <h3>Virkailijan opintopolku: käyttöoikeusanomuksia saapunut</h3>
        <p>
            Hei Heiki,
        </p>
        <p>
            Sinulle on saapunut uusia käyttöoikeusanomuksia.
        </p>
        <p>
            Voit jatkaa palveluun tästä: <a href="http://testilinkki.fi/anomukset">http://testilinkki.fi/anomukset</a>
        </p>
    </div>
    <div class="box" style="text-align: right;">
        <img src="http://www.oph.fi/instancedata/prime_product_julkaisu/oph/pics/opetushallitus2.gif" alt="Opetushallitus" />
    </div>
</body>
</html>""");
    }

    @Test
    @WithMockUser(username = "user1")
    public void NewRequisitionNotificationNoWorkEmailTest() {
        given(oppijanumerorekisteriClient.getHenkiloByOid(HENKILO_OID)).willReturn(new HenkiloDto());

        emailService.sendNewRequisitionNotificationEmails(Collections.singleton(HENKILO_OID));

        verify(queueingEmailService, never()).queueEmail(any());
    }

    @Test
    public void sendEmailKayttooikeusAnomusKasitelty() {
        HenkiloDto henkiloDto = new HenkiloDto();
        henkiloDto.setOidHenkilo(HENKILO_OID);
        henkiloDto.setYhteystiedotRyhma(Sets.newHashSet(CreateUtil.createYhteystietoSahkoposti("arpa@kuutio.fi", "yhteystietotyyppi7"),
                CreateUtil.createYhteystietoSahkoposti("arpa2@kuutio.fi", YhteystietoUtil.TYOOSOITE),
                CreateUtil.createYhteystietoSahkoposti("arpa3@kuutio.fi", "yhteystietotyyppi3")));
        henkiloDto.setAsiointiKieli(new KielisyysDto("sv", "svenska"));
        henkiloDto.setEtunimet("arpa noppa");
        henkiloDto.setKutsumanimi("arpa");
        henkiloDto.setSukunimi("kuutio");
        given(oppijanumerorekisteriClient.getHenkiloByOid(HENKILO_OID)).willReturn(henkiloDto);
        given(kayttoOikeusRyhmaRepository.findById(10L)).willReturn(of(KayttoOikeusRyhma.builder()
                .tunniste("kayttooikeusryhmatunniste")
                .nimi(new TextGroup())
                .build()));
        given(queueingEmailService.queueEmail(any())).willReturn("id");
        LocalDate startDate = LocalDate.of(2017, 10, 10);
        LocalDate endDate = LocalDate.of(2017, 10, 9);
        UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto
                = new UpdateHaettuKayttooikeusryhmaDto(10L, "MYONNETTY", startDate, endDate, null);

        Henkilo henkilo = new Henkilo();
        henkilo.setOidHenkilo(HENKILO_OID);
        Anomus anomus = Anomus.builder().sahkopostiosoite("arpa@kuutio.fi")
                .henkilo(henkilo)
                .anomuksenTila(AnomuksenTila.KASITELTY)
                .hylkaamisperuste("Hyvä oli")
                .haettuKayttoOikeusRyhmas(Sets.newHashSet(HaettuKayttoOikeusRyhma.builder()
                        .kayttoOikeusRyhma(KayttoOikeusRyhma.builder()
                                .nimi(new TextGroup())
                                .tunniste("Käyttöoikeusryhma haettu").build())
                        .build()))
                .myonnettyKayttooikeusRyhmas(Sets.newHashSet(MyonnettyKayttoOikeusRyhmaTapahtuma.builder()
                        .kayttoOikeusRyhma(KayttoOikeusRyhma.builder()
                                .nimi(new TextGroup())
                                .tunniste("Käyttöoikeusryhmä").build())
                        .build()))
                .build();
        anomus.getHaettuKayttoOikeusRyhmas().stream().forEach(h -> h.getKayttoOikeusRyhma().setId(10L));

        emailService.sendEmailAnomusKasitelty(anomus, updateHaettuKayttooikeusryhmaDto, 10L);

        ArgumentCaptor<QueuedEmail> emailCaptor = ArgumentCaptor.forClass(QueuedEmail.class);
        verify(queueingEmailService, times(1)).queueEmail(emailCaptor.capture());
        QueuedEmail email = emailCaptor.getValue();
        assertThat(email.getRecipients().size()).isEqualTo(1);
        assertThat(email.getRecipients().get(0)).isEqualTo("arpa@kuutio.fi");
        assertThat(email.getBody()).isEqualToIgnoringWhitespace("""
<!doctype html>
<html lang="sv">
<head>
    <meta charset="utf-8">
    <title>Studieinfo för administratörer: behandling av anhållan om användarrätt</title>
    <style>
        body {
            background: #F6F4F0;
        }
        .box {
            background: #FFFFFF;
            padding: 1em 2em;
            margin: 2em 4em;
        }
    </style>
</head>
<body>
    <div class="box">
        <h3>Studieinfo för administratörer: behandling av anhållan om användarrätt</h3>
        <p>
            Hej arpa kuutio
        </p>
        <p>
            Din anhållan om användarrättigheter har behandlats.
        </p>
        <p>
            kayttooikeusryhmatunniste Beviljad
        </p>
        <p>
            Du kan fortsätta till tjänsten: <a href="http://testilinkki.fi">http://testilinkki.fi</a>.
        </p>
    </div>
    <div class="box" style="text-align: right;">
        <img src="http://www.oph.fi/instancedata/prime_product_julkaisu/oph/pics/opetushallitus2.gif" alt="Opetushallitus" />
    </div>
</body>
</html>""");
    }

    @Test
    public void sendInvitationEmail() {
        OrganisaatioPerustieto organisaatioPerustieto = new OrganisaatioPerustieto();
        organisaatioPerustieto.setNimi(new HashMap<String, String>() {{
            put("fi", "suomenkielinennimi");
        }});

        given(organisaatioClient.getOrganisaatioPerustiedotCachedOrRefetch(any()))
                .willReturn(Optional.of(organisaatioPerustieto));
        given(oppijanumerorekisteriClient.getHenkiloByOid(any()))
                .willReturn(HenkiloDto.builder().kutsumanimi("kutsun").sukunimi("kutsuja").build());
        given(queueingEmailService.queueEmail(any())).willReturn("id");
        Kutsu kutsu = Kutsu.builder()
                .kieliKoodi("fi")
                .sahkoposti("arpa@kuutio.fi")
                .salaisuus("salaisuushash")
                .etunimi("arpa")
                .sukunimi("kuutio")
                .saate(null)
                .organisaatiot(Sets.newHashSet(KutsuOrganisaatio.builder()
                        .organisaatioOid("1.2.3.4.1")
                        .ryhmat(Sets.newHashSet(KayttoOikeusRyhma.builder().nimi(new TextGroup()).build()))
                        .build()))
                .aikaleima(LocalDateTime.now())
                .build();

        emailService.sendInvitationEmail(kutsu);

        ArgumentCaptor<QueuedEmail> emailCaptor = ArgumentCaptor.forClass(QueuedEmail.class);
        verify(queueingEmailService, times(1)).queueEmail(emailCaptor.capture());
        QueuedEmail email = emailCaptor.getValue();
        assertThat(email.getRecipients().size()).isEqualTo(1);
        assertThat(email.getRecipients().get(0)).isEqualTo("arpa@kuutio.fi");
        assertThat(email.getBody()).containsIgnoringWhitespaces("""
        <h3>Virkailijan Opintopolku: kutsu palvelun käyttäjäksi</h3>
        <p>
            Hei arpa kuutio,
        </p>
        <p>
            sinut on kutsuttu virkailijaksi Opetushallinnon palvelukokonaisuuteen. Sinulle on annettu käyttöoikeudet alla olevan mukaisesti.
        </p>
        <p>
                <p>
                            <strong>suomenkielinennimi</strong>
                </p>

        <p>
            Päästäksesi käyttämään palvelua, sinun tulee rekisteröityä alla olevan linkin kautta ja tunnistautua vahvasti mobiilivarmenteen, pankkitunnusten tai varmennekortin avulla.
        </p>""");
        assertThat(email.getBody()).containsIgnoringWhitespaces("""
        <p>
            Rekisteröitymisen jälkeen palveluun kirjaudutaan osoitteessa <a href="https://virkailija.opintopolku.fi">https://virkailija.opintopolku.fi</a>.
        </p>
        <p>
            Kutsun lähetti kutsun kutsuja
        </p>
        <p>
            Kutsu on voimassa 16.11.2024 asti
        </p>
    </div>
    <div class="box" style="text-align: right;">
        <img src="http://www.oph.fi/instancedata/prime_product_julkaisu/oph/pics/opetushallitus2.gif" alt="Opetushallitus" />
    </div>
</body>
</html>""");
    }

    @Test
    public void sendInvitationEmailInFinnishIfAsiointikieliIsEnglish() {
        OrganisaatioPerustieto organisaatioPerustieto = new OrganisaatioPerustieto();
        organisaatioPerustieto.setNimi(new HashMap<String, String>() {{
            put("fi", "suomenkielinennimi");
        }});

        given(organisaatioClient.getOrganisaatioPerustiedotCachedOrRefetch(any()))
                .willReturn(Optional.of(organisaatioPerustieto));
        given(oppijanumerorekisteriClient.getHenkiloByOid(any()))
                .willReturn(HenkiloDto.builder().kutsumanimi("kutsun").sukunimi("kutsuja").build());
        given(queueingEmailService.queueEmail(any())).willReturn("id");
        Kutsu kutsu = Kutsu.builder()
                .kieliKoodi("en")
                .sahkoposti("arpa@kuutio.fi")
                .salaisuus("salaisuushash")
                .etunimi("arpa")
                .sukunimi("kuutio")
                .saate(null)
                .organisaatiot(Sets.newHashSet(KutsuOrganisaatio.builder()
                        .organisaatioOid("1.2.3.4.1")
                        .ryhmat(Sets.newHashSet(KayttoOikeusRyhma.builder().nimi(new TextGroup()).build()))
                        .build()))
                .aikaleima(LocalDateTime.now())
                .build();

        emailService.sendInvitationEmail(kutsu);

        ArgumentCaptor<QueuedEmail> emailCaptor = ArgumentCaptor.forClass(QueuedEmail.class);
        verify(queueingEmailService, times(1)).queueEmail(emailCaptor.capture());
        QueuedEmail email = emailCaptor.getValue();
        assertThat(email.getRecipients().size()).isEqualTo(1);
        assertThat(email.getRecipients().get(0)).isEqualTo("arpa@kuutio.fi");
        assertThat(email.getBody()).containsIgnoringWhitespaces("""
        <h3>Virkailijan Opintopolku: kutsu palvelun käyttäjäksi</h3>
        <p>
            Hei arpa kuutio,
        </p>
        <p>
            sinut on kutsuttu virkailijaksi Opetushallinnon palvelukokonaisuuteen. Sinulle on annettu käyttöoikeudet alla olevan mukaisesti.
        </p>
        <p>
                <p>
                            <strong>suomenkielinennimi</strong>
                </p>

        <p>
            Päästäksesi käyttämään palvelua, sinun tulee rekisteröityä alla olevan linkin kautta ja tunnistautua vahvasti mobiilivarmenteen, pankkitunnusten tai varmennekortin avulla.
        </p>""");
        assertThat(email.getBody()).containsIgnoringWhitespaces("""
        <p>
            Rekisteröitymisen jälkeen palveluun kirjaudutaan osoitteessa <a href="https://virkailija.opintopolku.fi">https://virkailija.opintopolku.fi</a>.
        </p>
        <p>
            Kutsun lähetti kutsun kutsuja
        </p>
        <p>
            Kutsu on voimassa 16.11.2024 asti
        </p>
    </div>
    <div class="box" style="text-align: right;">
        <img src="http://www.oph.fi/instancedata/prime_product_julkaisu/oph/pics/opetushallitus2.gif" alt="Opetushallitus" />
    </div>
</body>
</html>""");
    }

    @Test
    public void sendInvitationEmailAsServiceKutsuja() {
        OrganisaatioPerustieto organisaatioPerustieto = new OrganisaatioPerustieto();
        organisaatioPerustieto.setNimi(new HashMap<String, String>() {{
            put("fi", "suomenkielinennimi");
        }});

        String expectedKutsuja =  "Varda Info";

        given(organisaatioClient.getOrganisaatioPerustiedotCachedOrRefetch(any()))
                .willReturn(Optional.of(organisaatioPerustieto));
        given(oppijanumerorekisteriClient.getHenkiloByOid(any()))
                .willReturn(HenkiloDto.builder().kutsumanimi("kutsun").sukunimi("kutsuja").build());
        given(queueingEmailService.queueEmail(any())).willReturn("id");
        Kutsu kutsu = Kutsu.builder()
                .kieliKoodi("fi")
                .sahkoposti("arpa@kuutio.fi")
                .salaisuus("salaisuushash")
                .etunimi("arpa")
                .sukunimi("kuutio")
                .saate(null)
                .organisaatiot(Sets.newHashSet(KutsuOrganisaatio.builder()
                        .organisaatioOid("1.2.3.4.1")
                        .ryhmat(Sets.newHashSet(KayttoOikeusRyhma.builder().nimi(new TextGroup()).build()))
                        .build()))
                .aikaleima(LocalDateTime.of(2024, 11, 16, 0, 0))
                .build();

        emailService.sendInvitationEmail(kutsu, Optional.of(expectedKutsuja));

        ArgumentCaptor<QueuedEmail> emailCaptor = ArgumentCaptor.forClass(QueuedEmail.class);
        verify(queueingEmailService, times(1)).queueEmail(emailCaptor.capture());
        QueuedEmail email = emailCaptor.getValue();
        assertThat(email.getRecipients().size()).isEqualTo(1);
        assertThat(email.getRecipients().get(0)).isEqualTo("arpa@kuutio.fi");
        assertThat(email.getBody()).containsIgnoringWhitespaces("""
        <h3>Virkailijan Opintopolku: kutsu palvelun käyttäjäksi</h3>
        <p>
            Hei arpa kuutio,
        </p>
        <p>
            sinut on kutsuttu virkailijaksi Opetushallinnon palvelukokonaisuuteen. Sinulle on annettu käyttöoikeudet alla olevan mukaisesti.
        </p>
        <p>
                <p>
                            <strong>suomenkielinennimi</strong>
                </p>

        <p>
            Päästäksesi käyttämään palvelua, sinun tulee rekisteröityä alla olevan linkin kautta ja tunnistautua vahvasti mobiilivarmenteen, pankkitunnusten tai varmennekortin avulla.
        </p>""");
        assertThat(email.getBody()).containsIgnoringWhitespaces("""
        <p>
            Rekisteröitymisen jälkeen palveluun kirjaudutaan osoitteessa <a href="https://virkailija.opintopolku.fi">https://virkailija.opintopolku.fi</a>.
        </p>
        <p>
            Kutsun lähetti Varda Info
        </p>
        <p>
            Kutsu on voimassa 16.12.2024 asti
        </p>
    </div>
    <div class="box" style="text-align: right;">
        <img src="http://www.oph.fi/instancedata/prime_product_julkaisu/oph/pics/opetushallitus2.gif" alt="Opetushallitus" />
    </div>
</body>
</html>""");
    }

    @Test
    public void sendDiscardedInvitationNotificationSuccess() {
        given(queueingEmailService.queueEmail(any())).willReturn("id");

        Kutsu invitation = Kutsu.builder()
                .kieliKoodi(TEST_LANG)
                .sahkoposti(TEST_EMAIL)
                .etunimi(TEST_FIRST_NAME)
                .sukunimi(TEST_LAST_NAME)
                .build();

        emailService.sendDiscardNotification(invitation);

        ArgumentCaptor<QueuedEmail> emailCaptor = ArgumentCaptor.forClass(QueuedEmail.class);
        verify(queueingEmailService, times(1)).queueEmail(emailCaptor.capture());
        QueuedEmail email = emailCaptor.getValue();
        assertThat(email.getRecipients().size()).isEqualTo(1);
        assertThat(email.getRecipients().get(0)).isEqualTo("arpa@kuutio.fi");
        assertThat(email.getBody()).isEqualToIgnoringWhitespace("""
<!doctype html>
<html lang="fi">
<head>
    <meta charset="utf-8">
    <title>Kutsu Opintopolun virkailijapalveluun poistettiin automaattisesti</title>
    <style>
        body {
            background: #F6F4F0;
        }
        .box {
            background: #FFFFFF;
            padding: 1em 2em;
            margin: 2em 4em;
        }
    </style>
</head>
<body>
    <div class="box">
        <h3>Kutsu Opintopolun virkailijapalveluun poistettiin automaattisesti</h3>
        <p>
            Kutsu Opintopolun virkailijapalveluun poistettiin automaattisesti,
            sillä kutsulinkki on vanhentunut 1 kk sitten. Pyydä tarvittaessa
            uusi kutsu palveluun Opintopolun vastuukäyttäjältä tai palvelukohtaiselta
            pääkäyttäjältä. Mikäli sinulla on jo käyttäjätunnus ja käyttöoikeuksia
            Opintopolun virkailijapalveluun, ne säilyvät ennallaan.
        </p>
    </div>
    <div class="box" style="text-align: right;">
        <img src="http://www.oph.fi/instancedata/prime_product_julkaisu/oph/pics/opetushallitus2.gif" alt="Opetushallitus" />
    </div>
</body>
</html>""");
    }

    @Test
    public void sendDiscardedApplicationNotificationSuccess() {
        given(queueingEmailService.queueEmail(any())).willReturn("id");

        Henkilo henkilo = mock(Henkilo.class);
        when(henkilo.getKutsumanimiCached()).thenReturn(TEST_FIRST_NAME);
        when(henkilo.getSukunimiCached()).thenReturn(TEST_LAST_NAME);

        Anomus application = mock(Anomus.class);
        when(application.getHenkilo()).thenReturn(henkilo);
        when(application.getSahkopostiosoite()).thenReturn(TEST_EMAIL);

        KielisyysDto kielisyysDto = mock(KielisyysDto.class);
        when(kielisyysDto.getKieliKoodi()).thenReturn(TEST_LANG);

        HenkiloDto henkiloDto = mock(HenkiloDto.class);
        when(henkiloDto.getAsiointiKieli()).thenReturn(kielisyysDto);

        given(oppijanumerorekisteriClient.getHenkiloByOid(any())).willReturn(henkiloDto);

        emailService.sendDiscardNotification(application);

        ArgumentCaptor<QueuedEmail> emailCaptor = ArgumentCaptor.forClass(QueuedEmail.class);
        verify(queueingEmailService, times(1)).queueEmail(emailCaptor.capture());
        QueuedEmail email = emailCaptor.getValue();
        assertThat(email.getRecipients().size()).isEqualTo(1);
        assertThat(email.getRecipients().get(0)).isEqualTo("arpa@kuutio.fi");
        assertThat(email.getBody()).isEqualToIgnoringWhitespace("""
<!doctype html>
<html lang="fi">
<head>
    <meta charset="utf-8">
    <title>Virkailijan opintopolku: käyttöoikeusanomus hylättiin automaattisesti</title>
    <style>
        body {
            background: #F6F4F0;
        }
        .box {
            background: #FFFFFF;
            padding: 1em 2em;
            margin: 2em 4em;
        }
    </style>
</head>
<body>
    <div class="box">
        <h3>Virkailijan opintopolku: käyttöoikeusanomus hylättiin automaattisesti</h3>
        <p>
            Tekemäsi käyttöoikeusanomus virkailijan Opintopolkuun hylättiin automaattisesti,
            koska vastuu- tai pääkäyttäjä ei ole käsitellyt sitä kahden kuukauden sisällä.
            Hylkäämisellä ei ole vaikutusta voimassa oleviin käyttöoikeuksiin tai uusiin anomuksiin.
            Ano tarvittaessa käyttöoikeutta uudelleen osoitteessa
            <a href="https://virkailija.opintopolku.fi/henkilo-ui/omattiedot">https://virkailija.opintopolku.fi/henkilo-ui/omattiedot</a>.
            Lisätietoja: <a href="mailto:yhteisetpalvelut@opintopolku.fi">yhteisetpalvelut@opintopolku.fi</a>
        </p>
    </div>
    <div class="box" style="text-align: right;">
        <img src="http://www.oph.fi/instancedata/prime_product_julkaisu/oph/pics/opetushallitus2.gif" alt="Opetushallitus" />
    </div>
</body>
</html>""");
    }
}
