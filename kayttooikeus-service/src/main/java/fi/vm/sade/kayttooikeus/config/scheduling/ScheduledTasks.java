package fi.vm.sade.kayttooikeus.config.scheduling;

import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.ScheduleTimestampsDataRepository;
import fi.vm.sade.kayttooikeus.service.*;
import fi.vm.sade.kayttooikeus.service.exception.DataInconsistencyException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Ajastusten konfigurointi.
 *
 * @see SchedulingConfiguration ajastuksen aktivointi
 */
@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledTasks.class);

    private final OrganisaatioService organisaatioService;
    private final MyonnettyKayttoOikeusService myonnettyKayttoOikeusService;
    private final TaskExecutorService taskExecutorService;
    private final KayttooikeusAnomusService kayttooikeusAnomusService;
    private final LdapSynchronizationService ldapSynchronizationService;
    private final HenkiloCacheService henkiloCacheService;

    private final HenkiloDataRepository henkiloDataRepository;
    private final ScheduleTimestampsDataRepository scheduleTimestampsDataRepository;

    private final CommonProperties commonProperties;

    @Scheduled(cron = "${kayttooikeus.scheduling.configuration.organisaatiocache}")
    public void updateOrganisaatioCache() {
        organisaatioService.updateOrganisaatioCache();
    }

    @Scheduled(cron = "${kayttooikeus.scheduling.configuration.vanhentuneetkayttooikeudet}")
    public void poistaVanhentuneetKayttoOikeudet() {
        myonnettyKayttoOikeusService.poistaVanhentuneet(commonProperties.getAdminOid());
    }

    @Scheduled(cron = "${kayttooikeus.scheduling.configuration.kayttooikeusmuistutus}")
    public void sendExpirationReminders() {
        taskExecutorService.sendExpirationReminders(Period.ofWeeks(4), Period.ofWeeks(1));
    }

    @Scheduled(cron = "${kayttooikeus.scheduling.configuration.kayttooikeusanomusilmoitukset}")
    public void lahetaUusienAnomuksienIlmoitukset() {
        kayttooikeusAnomusService.lahetaUusienAnomuksienIlmoitukset(LocalDate.now().minusDays(1));
    }

    @Scheduled(fixedDelayString = "${kayttooikeus.scheduling.ldapsynkronointi.fixeddelayinmillis}",
            initialDelayString = "${kayttooikeus.scheduling.ldapsynkronointi.initialdelayinmillis}")
    public void ldapSynkronointi() {
        ldapSynchronizationService.runSynchronizer();
    }

    @Scheduled(fixedDelayString = "${kayttooikeus.scheduling.configuration.henkiloNimiCache}")
    @Transactional
    public void updateHenkiloNimiCache() {
        if(this.henkiloDataRepository.countByEtunimetCachedNotNull() > 0L) {
            this.henkiloCacheService.updateHenkiloCache();
        }
        // Fetch whole henkilo nimi cache
        else {
            Long count = 1000L;
            for(long page = 0; !this.henkiloCacheService.saveAll(page*count, count, null); page++) {
                // Escape condition in case of inifine loop (10M+ henkilos)
                if(page > 10000) {
                    LOG.error("Infinite loop detected with page "+ page + " and count " + count + ". Henkilo cache might not be fully updated!");
                    break;
                }
            }
            this.scheduleTimestampsDataRepository.findFirstByIdentifier("henkilocache")
                    .orElseThrow(DataInconsistencyException::new)
                    .setModified(LocalDateTime.now());
        }
    }
}
