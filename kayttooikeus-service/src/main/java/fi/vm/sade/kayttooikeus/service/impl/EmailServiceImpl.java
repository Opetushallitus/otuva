package fi.vm.sade.kayttooikeus.service.impl;

import com.google.common.collect.Lists;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.config.properties.EmailInvitationProperties;
import fi.vm.sade.kayttooikeus.dto.YhteystietojenTyypit;
import fi.vm.sade.kayttooikeus.model.Anomus;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaRepository;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.service.EmailService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.RyhmasahkopostiClient;
import fi.vm.sade.kayttooikeus.util.AnomusKasiteltySahkopostiBuilder;
import fi.vm.sade.kayttooikeus.util.UserDetailsUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloPerustietoDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkilonYhteystiedotViewDto;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailData;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailMessage;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailRecipient;
import fi.vm.sade.ryhmasahkoposti.api.dto.ReportedRecipientReplacementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static fi.vm.sade.kayttooikeus.service.impl.KutsuServiceImpl.CALLING_PROCESS;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final String PROSESSI = "kayttooikeus";
    private static final String DEFAULT_LANGUAGE_CODE = "fi";
    private static final Locale DEFAULT_LOCALE = new Locale(DEFAULT_LANGUAGE_CODE);
    private static final String KAYTTOOIKEUSMUISTUTUS_EMAIL_TEMPLATE_NAME = "kayttooikeusmuistutus_email";
    private static final String ANOMUS_KASITELTY_EMAIL_TEMPLATE_NAME = "kayttooikeusanomuskasitelty_email";
    private static final String ANOMUS_KASITELTY_EMAIL_REPLACEMENT_TILA = "anomuksenTila";
    private static final String ANOMUS_KASITELTY_EMAIL_REPLACEMENT_PERUSTE = "hylkaamisperuste";
    private static final String ANOMUS_KASITELTY_EMAIL_REPLACEMENT_ROOLIT = "roolit";

    private final String expirationReminderSenderEmail;
    private final String expirationReminderPersonUrl;
    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;
    private final RyhmasahkopostiClient ryhmasahkopostiClient;

    private final KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository;


    @Autowired
    public EmailServiceImpl(OppijanumerorekisteriClient oppijanumerorekisteriClient,
                            RyhmasahkopostiClient ryhmasahkopostiClient,
                            EmailInvitationProperties config,
                            OphProperties ophProperties,
                            KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository) {
        this.oppijanumerorekisteriClient = oppijanumerorekisteriClient;
        this.ryhmasahkopostiClient = ryhmasahkopostiClient;
        this.expirationReminderSenderEmail = config.getSenderEmail();
        this.expirationReminderPersonUrl = ophProperties.url("authentication-henkiloui.expirationreminder.personurl");
        this.kayttoOikeusRyhmaRepository = kayttoOikeusRyhmaRepository;
    }

    @Override
    public void sendEmailAnomusAccepted(Anomus anomus) {
        // add all handled with accepted status to email
        this.sendEmailAnomusHandled(anomus, input -> input.myonnetyt(anomus.getMyonnettyKayttooikeusRyhmas()));
    }

    private void sendEmailAnomusHandled(Anomus anomus, Consumer<AnomusKasiteltySahkopostiBuilder> consumer) {
        /* If this fails, the whole requisition handling process MUST NOT fail!!
         * Having an email sent from handled requisitions is a nice-to-have feature
         * but it's not a reason to cancel the whole transaction
         */
        try {
            EmailRecipient recipient = this.newEmailRecipient(anomus.getHenkilo());
            recipient.setEmail(anomus.getSahkopostiosoite());
            String languageCode = recipient.getLanguageCode();

            List<ReportedRecipientReplacementDTO> replacements = new ArrayList<>();
            replacements.add(new ReportedRecipientReplacementDTO(ANOMUS_KASITELTY_EMAIL_REPLACEMENT_TILA, anomus.getAnomuksenTila()));
            replacements.add(new ReportedRecipientReplacementDTO(ANOMUS_KASITELTY_EMAIL_REPLACEMENT_PERUSTE, anomus.getHylkaamisperuste()));
            AnomusKasiteltySahkopostiBuilder builder = new AnomusKasiteltySahkopostiBuilder(this.kayttoOikeusRyhmaRepository, languageCode);
            consumer.accept(builder);
            replacements.add(new ReportedRecipientReplacementDTO(ANOMUS_KASITELTY_EMAIL_REPLACEMENT_ROOLIT, builder.build()));
            recipient.setRecipientReplacements(replacements);

            EmailMessage message = this.generateEmailMessage(ANOMUS_KASITELTY_EMAIL_TEMPLATE_NAME, languageCode);
            this.ryhmasahkopostiClient.sendRyhmasahkoposti(new EmailData(Lists.newArrayList(recipient), message));
        }
        catch (Exception e) {
            logger.error("Error sending requisition handled email", e);
        }
    }

    private EmailMessage generateEmailMessage(String templateName, String languageCode) {
        return generateEmailMessage(languageCode, this.expirationReminderSenderEmail,
                this.expirationReminderSenderEmail, templateName);
    }

    private EmailMessage generateEmailMessage(String languageCode, String fromEmail, String replyToEmail, String templateName) {
        EmailMessage message = new EmailMessage();
        message.setCallingProcess(CALLING_PROCESS);
        message.setFrom(fromEmail);
        message.setReplyTo(replyToEmail);
        message.setTemplateName(templateName);
        message.setHtml(true);
        message.setLanguageCode(languageCode);
        return message;
    }

    private EmailRecipient newEmailRecipient(Henkilo henkilo) {
        HenkiloDto henkiloDto = this.oppijanumerorekisteriClient.getHenkiloByOid(henkilo.getOidHenkilo());
        String email = UserDetailsUtil.getEmailByPriority(henkiloDto)
                .orElseThrow(() -> new NotFoundException("User has no valid email"));
        EmailRecipient recipient = new EmailRecipient();
        recipient.setEmail(email);
        recipient.setOid(henkilo.getOidHenkilo());
        recipient.setOidType("henkilo");
        recipient.setName(UserDetailsUtil.getName(henkiloDto));
        recipient.setLanguageCode(UserDetailsUtil.getLanguageCode(henkiloDto));
        return recipient;
    }

    @Override
    @Transactional
    public void sendExpirationReminder(String henkiloOid, List<ExpiringKayttoOikeusDto> tapahtumas) {
        // Not grouped by language code since might change to one TX / receiver in the future.
        
        HenkiloPerustietoDto henkilonPerustiedot = oppijanumerorekisteriClient.getHenkilonPerustiedot(henkiloOid)
                .orElseThrow(() -> new NotFoundException("Henkilö not found by henkiloOid=" + henkiloOid));
        String langugeCode = getLanguageCode(henkilonPerustiedot);
        
        EmailData email = new EmailData();
        email.setEmail(getEmailMessage(langugeCode));
        email.setRecipient(singletonList(getEmailRecipient(henkiloOid, langugeCode, tapahtumas)));
        ryhmasahkopostiClient.sendRyhmasahkoposti(email);
    }

    private EmailRecipient getEmailRecipient(String henkiloOid, String langugeCode, List<ExpiringKayttoOikeusDto> kayttoOikeudet) {
        HenkilonYhteystiedotViewDto yhteystiedot = oppijanumerorekisteriClient.getHenkilonYhteystiedot(henkiloOid);
        String email = yhteystiedot.get(YhteystietojenTyypit.PRIORITY_ORDER).getSahkoposti();
        if (email == null) {
            logger.warn("Henkilöllä (oid={}) ei ole sähköpostia yhteystietona. Käyttöoikeuksien vanhentumisesta ei lähetetty muistutusta", henkiloOid);
            return null;
        }

        List<ReportedRecipientReplacementDTO> replacements = new ArrayList<>();
        replacements.add(new ReportedRecipientReplacementDTO("expirations", getExpirationsText(kayttoOikeudet, langugeCode)));
        replacements.add(new ReportedRecipientReplacementDTO("url", expirationReminderPersonUrl));

        EmailRecipient recipient = new EmailRecipient(henkiloOid, email);
        recipient.setLanguageCode(langugeCode);
        recipient.setRecipientReplacements(replacements);

        return recipient;
    }

    private EmailMessage getEmailMessage(String languageCode) {
        EmailMessage message = new EmailMessage();
        message.setCallingProcess(PROSESSI);
        message.setFrom(expirationReminderSenderEmail);
        message.setReplyTo(expirationReminderSenderEmail);
        message.setTemplateName(KAYTTOOIKEUSMUISTUTUS_EMAIL_TEMPLATE_NAME);
        message.setHtml(true);
        message.setLanguageCode(languageCode);
        return message;
    }

    private String getLanguageCode(HenkiloPerustietoDto henkilo) {
        return ofNullable(henkilo.getAsiointiKieli()).flatMap(k -> ofNullable(k.getKieliKoodi()))
                .orElse(DEFAULT_LANGUAGE_CODE);
    }
    
    private String getExpirationsText(List<ExpiringKayttoOikeusDto> kayttoOikeudet, String languageCode) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, DEFAULT_LOCALE);
        return kayttoOikeudet.stream().map(kayttoOikeus -> {
            String kayttoOikeusRyhmaNimi = ofNullable(kayttoOikeus.getRyhmaDescription())
                    .flatMap(d -> d.getOrAny(languageCode)).orElse(kayttoOikeus.getRyhmaName());
            String voimassaLoppuPvmStr = dateFormat.format(kayttoOikeus.getVoimassaLoppuPvm().toDate());
            return String.format("%s (%s)", kayttoOikeusRyhmaNimi, voimassaLoppuPvmStr);
        }).collect(joining(", "));
    }
}
