package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.model.Anomus;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.model.KutsuOrganisaatio;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaRepository;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.service.EmailService;
import fi.vm.sade.kayttooikeus.service.QueueingEmailService;
import fi.vm.sade.kayttooikeus.service.QueueingEmailService.QueuedEmail;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.util.LocalisationUtils;
import fi.vm.sade.kayttooikeus.util.UserDetailsUtil;
import fi.vm.sade.kayttooikeus.util.YhteystietoUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.DateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.ui.freemarker.FreeMarkerTemplateUtils.processTemplateIntoString;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceViestinvalitysImpl implements EmailService {
    private static final String DEFAULT_LANGUAGE_CODE = "fi";
    private static final List<String> SUPPORTED_ASIOINTIKIELI = List.of("fi", "sv");

    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;
    private final QueueingEmailService queueingEmailService;
    private final OrganisaatioClient organisaatioClient;
    private final KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository;
    private final Configuration freemarker;

    @Value("${url-virkailija}")
    private String urlVirkailija;
    @Value("${cas.oppija.login}")
    private String casOppijaLogin;

    @Data
    @Builder
    public static class AnomusKasitelty {
        HenkiloDto henkiloDto;
        AnomusKasiteltyRecipientDto rooli;
        String linkki;
        String subject;
    };

    @Override
    public void sendEmailAnomusKasitelty(Anomus anomus, UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto, Long kayttooikeusryhmaId) {
        try {
            HenkiloDto henkiloDto = oppijanumerorekisteriClient.getHenkiloByOid(anomus.getHenkilo().getOidHenkilo());
            String language = UserDetailsUtil.getLanguageCode(henkiloDto, "fi", "sv");
            String subject = "sv".equals(language)
                ? "Studieinfo för administratörer: behandling av anhållan om användarrätt"
                : "Virkailijan opintopolku: käyttöoikeusanomus käsitelty";
            AnomusKasitelty anomusKasitelty = AnomusKasitelty.builder()
                .henkiloDto(henkiloDto)
                .rooli(createAnomusKasiteltyDto(anomus, updateHaettuKayttooikeusryhmaDto, language, kayttooikeusryhmaId))
                .linkki(urlVirkailija)
                .subject(subject)
                .build();

            Template template = freemarker.getTemplate("emails/anomuskasitelty_" + language + ".ftl");
            QueuedEmail email = QueuedEmail.builder()
                .subject(subject)
                .recipients(List.of(anomus.getSahkopostiosoite()))
                .body(processTemplateIntoString(template, anomusKasitelty))
                .build();

            String emailId = queueingEmailService.queueEmail(email);
            queueingEmailService.attemptSendingEmail(emailId);
        } catch (Exception e) {
            log.error("Error sending anomus email for anomus " + anomus.getId(), e);
        }
    }

    private AnomusKasiteltyRecipientDto createAnomusKasiteltyDto(Anomus anomus, UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusDto, String languageCode, Long kayttooikeusryhmaId) {
        KayttoOikeusRyhma kayttooikeusryhma = kayttoOikeusRyhmaRepository.findById(kayttooikeusryhmaId).orElseThrow(() -> new NotFoundException("Käyttöoikeusryhmää ei löytynyt id:llä: " + kayttooikeusryhmaId));
        String kayttooikeusryhmaNimi = LocalisationUtils.getText(languageCode, kayttooikeusryhma.getNimi(), kayttooikeusryhma::getTunniste);
        if (KayttoOikeudenTila.valueOf(updateHaettuKayttooikeusDto.getKayttoOikeudenTila()) == KayttoOikeudenTila.MYONNETTY) {
            return new AnomusKasiteltyRecipientDto(kayttooikeusryhmaNimi, KayttoOikeudenTila.MYONNETTY);
        }
        return new AnomusKasiteltyRecipientDto(kayttooikeusryhmaNimi, KayttoOikeudenTila.HYLATTY, updateHaettuKayttooikeusDto.getHylkaysperuste());
    }

    @Data
    @Builder
    public static class VanhenemisMuistutus {
        HenkiloDto henkiloDto;
        String kayttooikeusryhmat;
        String linkki;
        String subject;
    };

    @Override
    @Transactional
    public void sendExpirationReminder(String henkiloOid, List<ExpiringKayttoOikeusDto> tapahtumas) {
        try {
            HenkiloDto henkiloDto = oppijanumerorekisteriClient.getHenkiloByOid(henkiloOid);
            String recipient = YhteystietoUtil.getWorkEmail(henkiloDto.getYhteystiedotRyhma())
                .orElseThrow(() -> new NotFoundException("Henkilö not found by henkiloOid=" + henkiloOid));
            String language = UserDetailsUtil.getLanguageCode(henkiloDto, "fi", "sv");
            String subject = "sv".equals(language)
                ? "Studieinfo för administratörer: användarrättigheter utgår inom kort"
                : "Virkailijan opintopolku: käyttöoikeuksia vanhenemassa";
            VanhenemisMuistutus vanhenemisMuistutus = VanhenemisMuistutus.builder()
                .henkiloDto(henkiloDto)
                .kayttooikeusryhmat(getExpirationsText(tapahtumas, language))
                .linkki(urlVirkailija + "/henkilo-ui/omattiedot")
                .subject(subject)
                .build();

            Template template = freemarker.getTemplate("emails/vanhenemismuistutus_" + language + ".ftl");
            QueuedEmail email = QueuedEmail.builder()
                .subject(subject)
                .recipients(List.of(recipient))
                .body(processTemplateIntoString(template, vanhenemisMuistutus))
                .build();

            String emailId = queueingEmailService.queueEmail(email);
            queueingEmailService.attemptSendingEmail(emailId);
        } catch (Exception e) {
            log.error("Error sending expiration reminder email", e);
        }
    }

    private String getExpirationsText(List<ExpiringKayttoOikeusDto> kayttoOikeudet, String languageCode) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.of(DEFAULT_LANGUAGE_CODE));
        return kayttoOikeudet.stream().map(kayttoOikeus -> {
            String kayttoOikeusRyhmaNimi = ofNullable(kayttoOikeus.getRyhmaDescription())
                    .flatMap(d -> d.getOrAny(languageCode)).orElse(kayttoOikeus.getRyhmaName());
            String voimassaLoppuPvmStr = dateFormat.format(Date.from(kayttoOikeus.getVoimassaLoppuPvm().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            return String.format("%s (%s)", kayttoOikeusRyhmaNimi, voimassaLoppuPvmStr);
        }).collect(joining(", "));
    }

    @Data
    @Builder
    public static class HakemusIlmoitus {
        HenkiloDto henkiloDto;
        String linkki;
        String subject;
    };

    @Override
    @Transactional
    public void sendNewRequisitionNotificationEmails(Set<String> henkiloOids) {
        henkiloOids.stream().forEach(oid -> {
            try {
                HenkiloDto henkiloDto = oppijanumerorekisteriClient.getHenkiloByOid(oid);
                var recipient = YhteystietoUtil.getWorkEmail(henkiloDto.getYhteystiedotRyhma());
                if (recipient.isEmpty()) {
                    log.info("Could not send email (missing work email) to " + oid);
                    return;
                }

                String language = UserDetailsUtil.getLanguageCode(henkiloDto, "fi", "sv");
                String subject = "sv".equals(language)
                    ? "Studieinfo för administratörer: nya användarrättigheter att behandla"
                    : "Virkailijan opintopolku: käyttöoikeusanomuksia saapunut";
                HakemusIlmoitus hakemusIlmoitus = HakemusIlmoitus.builder()
                    .henkiloDto(henkiloDto)
                    .linkki(urlVirkailija + "/henkilo-ui/anomukset")
                    .subject(subject)
                    .build();

                Template template = freemarker.getTemplate("emails/hakemusilmoitus_" + language + ".ftl");
                QueuedEmail email = QueuedEmail.builder()
                    .subject(subject)
                    .recipients(List.of(recipient.get()))
                    .body(processTemplateIntoString(template, hakemusIlmoitus))
                    .build();

                String emailId = queueingEmailService.queueEmail(email);
                queueingEmailService.attemptSendingEmail(emailId);
            } catch (Exception e) {
                log.error("Error new requisition notification email", e);
            }
        });
    }

    @Data
    @Builder
    public static class TemplateOrganisation {
        String name;
        List<String> permissions;
    };

    @Data
    @Builder
    public static class KutsuEmail {
        Kutsu kutsu;
        String kutsuja;
        String voimassa;
        List<TemplateOrganisation> organisaatiot;
        String linkki;
        String subject;
    };

    @Override
    public void sendInvitationEmail(Kutsu kutsu) {
        sendInvitationEmail(kutsu, Optional.empty());
    }

    @Override
    public void sendInvitationEmail(Kutsu kutsu, Optional<String> inviterOverride) {
        try {
            String language = validateLanguage(kutsu.getKieliKoodi());
            String subject = "sv".equals(language)
                ? "Studieinfo för administratörer: inbjudan till användare av tjänsten"
                : "Virkailijan Opintopolku: kutsu palvelun käyttäjäksi";

            var organisaatiot = kutsu.getOrganisaatiot().stream()
                    .map(org -> toTemplateOrganisation(org, language))
                    .sorted(comparing(TemplateOrganisation::getName))
                    .toList();

            KutsuEmail VanhenemisMuistutus = KutsuEmail.builder()
                .kutsu(kutsu)
                .linkki(getKutsuLink(kutsu))
                .kutsuja(inviterOverride.orElseGet(() -> resolveInviterName(kutsu)))
                .voimassa(kutsu.getAikaleima().plusMonths(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .organisaatiot(organisaatiot)
                .subject(subject)
                .build();

            Template template = freemarker.getTemplate("emails/kutsu_" + language + ".ftl");
            QueuedEmail email = QueuedEmail.builder()
                .subject(subject)
                .recipients(List.of(kutsu.getSahkoposti()))
                .body(processTemplateIntoString(template, VanhenemisMuistutus))
                .build();

            log.info("Sending invitation email to {}", kutsu.getSahkoposti());
            String emailId = queueingEmailService.queueEmail(email);
            queueingEmailService.attemptSendingEmail(emailId);
        } catch (Exception e) {
            log.error("Error on kutsu email", e);
        }
    }

    private String getKutsuLink(Kutsu kutsu) {
        String language = kutsu.getKieliKoodi().toLowerCase();
        String targetUri = UriComponentsBuilder.fromUriString(urlVirkailija + "/kayttooikeus-service/cas/tunnistus")
                .queryParam("kutsuToken", kutsu.getSalaisuus())
                .queryParam("locale", language)
                .build()
                .toUriString();
        return UriComponentsBuilder.fromUriString(casOppijaLogin)
                .queryParam("service", URLEncoder.encode(targetUri, StandardCharsets.UTF_8))
                .queryParam("locale", language.toUpperCase())
                .build()
                .toUriString();
    }

    private TemplateOrganisation toTemplateOrganisation(KutsuOrganisaatio org, String language) {
        var name = new TextGroupMapDto(
                organisaatioClient.getOrganisaatioPerustiedotCachedOrRefetch(org.getOrganisaatioOid())
                    .orElseThrow(() -> new NotFoundException("Organisation not found with oid " + org.getOrganisaatioOid()))
                    .getNimi()
            ).getOrAny(language).orElse(null);
        var permissions = org.getRyhmat().stream()
            .map(KayttoOikeusRyhma::getNimi)
            .map(desc -> desc.getOrAny(language).orElse(null))
            .filter(Objects::nonNull)
            .sorted()
            .toList();
        return new TemplateOrganisation(name, permissions);
    }

    private String resolveInviterName(Kutsu kutsu) {
        HenkiloDto kutsuja = oppijanumerorekisteriClient.getHenkiloByOid(kutsu.getKutsuja());
        return String.format("%s %s", kutsuja.getKutsumanimi(), kutsuja.getSukunimi());
    }

    @Data
    @Builder
    public static class KutsuPoistettu {
        String subject;
    };

    @Override
    public void sendDiscardNotification(Kutsu invitation) {
        try {
            String language = validateLanguage(invitation.getKieliKoodi());
            String subject = "sv".equals(language)
                ? "Inbjudan till Studieinfo för administratörer makulerades automatiskt"
                : "Kutsu Opintopolun virkailijapalveluun poistettiin automaattisesti";

            Template template = freemarker.getTemplate("emails/kutsupoistettu_" + language + ".ftl");
            QueuedEmail email = QueuedEmail.builder()
                .subject(subject)
                .recipients(List.of(invitation.getSahkoposti()))
                .body(processTemplateIntoString(template, new KutsuPoistettu(subject)))
                .build();

            String emailId = queueingEmailService.queueEmail(email);
            queueingEmailService.attemptSendingEmail(emailId);
        } catch (Exception e) {
            log.error("Error sending requisition email", e);
        }
    }

    @Data
    @Builder
    public static class AnomusPoistettu {
        String subject;
    };

    @Override
    public void sendDiscardNotification(Anomus application) {
        try {
            HenkiloDto henkiloDto = oppijanumerorekisteriClient.getHenkiloByOid(application.getHenkilo().getOidHenkilo());
            String language = UserDetailsUtil.getLanguageCode(henkiloDto, "fi", "sv");
            String subject = "sv".equals(language)
                ? "Studieinfo för administratörer: anhållan om användarrättighet avslogs automatiskt"
                : "Virkailijan opintopolku: käyttöoikeusanomus hylättiin automaattisesti";

            Template template = freemarker.getTemplate("emails/anomuspoistettu_" + language + ".ftl");
            QueuedEmail email = QueuedEmail.builder()
                .subject(subject)
                .recipients(List.of(application.getSahkopostiosoite()))
                .body(processTemplateIntoString(template, new AnomusPoistettu(subject)))
                .build();

            String emailId = queueingEmailService.queueEmail(email);
            queueingEmailService.attemptSendingEmail(emailId);
        } catch (Exception e) {
            log.error("Error sending requisition email", e);
        }
    }

    private String validateLanguage(String language) {
        return language != null && SUPPORTED_ASIOINTIKIELI.contains(language.toLowerCase())
            ? language.toLowerCase()
            : DEFAULT_LANGUAGE_CODE;
    }
}
