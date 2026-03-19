package fi.vm.sade.kayttooikeus;

import fi.vm.sade.kayttooikeus.dto.KayttajaTyyppi;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.model.Oauth2Client;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.Oauth2ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@ConditionalOnProperty(name = "feature.local-authentication")
@Component
@RequiredArgsConstructor
public class LocalOauth2ClientManager implements ApplicationListener<ApplicationReadyEvent> {

    private final Oauth2ClientRepository oauth2ClientRepository;
    private final HenkiloDataRepository henkiloRepository;
    private final PasswordEncoder passwordEncoder;

    private final String casOppijaHenkiloOid = "1.2.246.562.24.71416296437";

    private final String kayttooikeusHenkiloOid = "1.2.246.562.24.53190251392";

    @Value("${cas.oppija.oauth2.id}")
    private String casOppijaOAuth2Id;

    @Value("${cas.oppija.oauth2.secret}")
    private String casOppijaOAuth2Secret;

    @Value("${cas.oppija.oauth2.uuid}")
    private UUID casOppijaOAuth2Uuid;

    @Value("${kayttooikeus.palvelukayttaja.client-id}")
    private String kayttooikeusClientId;

    @Value("${kayttooikeus.palvelukayttaja.client-secret}")
    private String kayttooikeusClientSecret;

    @Value("${kayttooikeus.palvelukayttaja.client-uuid}")
    private UUID kayttooikeusClientUuid;

    private void addOauth2Client(String clientId, String secret, UUID uuid) {
        var client = Oauth2Client.builder()
                .id(clientId)
                .secret(passwordEncoder.encode(secret))
                .updated(LocalDateTime.now())
                .created(LocalDateTime.now())
                .uuid(uuid)
                .build();
        oauth2ClientRepository.save(client);
    }

    private void addServiceClientKayttajatiedot(String henkiloOid, String clientId) {
        var person = Henkilo.builder()
                .oidHenkilo(henkiloOid)
                .kayttajaTyyppi(KayttajaTyyppi.PALVELU)
                .build();
        var userInfo = Kayttajatiedot.builder()
                .username(clientId)
                .henkilo(person)
                .invalidated(false)
                .build();
        person.setKayttajatiedot(userInfo);
        henkiloRepository.save(person);
    }

    private void addKayttooikeusServiceOauth2Client() {
        log.info("Checking if kayttooikeus-service has a registered OAuth2 client");
        var existing = oauth2ClientRepository.findById(kayttooikeusClientId);
        log.debug("kayttooikeus-service has OAuth2 client: " + existing.isPresent());
        if (existing.isEmpty()) {
            log.info("Adding OAuth2 client for kayttooikeus-service");
            addServiceClientKayttajatiedot(kayttooikeusHenkiloOid, kayttooikeusClientId);
            addOauth2Client(kayttooikeusClientId, kayttooikeusClientSecret, kayttooikeusClientUuid);
            log.info("OAuth2 client added for kayttooikeus-service");
        }
    }

    private void addCasOppijaOauth2Client() {
        log.info("Checking if cas-oppija has a registered OAuth2 client");
        var existing = oauth2ClientRepository.findById(casOppijaOAuth2Id);
        log.debug("cas-oppija has OAuth2 client: " + existing.isPresent());
        if (existing.isEmpty()) {
            log.info("Adding OAuth2 client for cas-oppija");
            addServiceClientKayttajatiedot(casOppijaHenkiloOid, casOppijaOAuth2Id);
            addOauth2Client(casOppijaOAuth2Id, casOppijaOAuth2Secret, casOppijaOAuth2Uuid);
            log.info("OAuth2 client added for cas-oppija");
        }
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Application ready, initializing, feature.local-authentication turned on (true)");
        addCasOppijaOauth2Client();
        addKayttooikeusServiceOauth2Client();
        log.info("Initializing complete");
    }
}
