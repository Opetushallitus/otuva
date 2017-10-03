package fi.vm.sade.kayttooikeus.config.scheduling;

import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.model.ScheduleTimestamps;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.ScheduleTimestampsDataRepository;
import fi.vm.sade.kayttooikeus.service.*;
import fi.vm.sade.kayttooikeus.service.exception.DataInconsistencyException;
import fi.vm.sade.kayttooikeus.service.external.ExternalServiceException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Ajastusten konfigurointi.
 *
 * @see SchedulingConfiguration ajastuksen aktivointi
 */
@Component
@Slf4j
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

    private final OrganisaatioClient organisaatioClient;
    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;

    private final CommonProperties commonProperties;
    @Value("${kayttooikeus.scheduling.run-on-startup}")
    private Boolean schedulingEnabledOnStartup;

    @PostConstruct
    public void onStartup() {
        if(BooleanUtils.isTrue(this.schedulingEnabledOnStartup)) {
            try {
                this.organisaatioClient.refreshCache();
            } catch (ExternalServiceException e) {
                log.error("Could not refresh organisation cache. Organisation service not responding.", e);
            }
        }
    }

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
        // Update existing cache
        if (this.henkiloDataRepository.countByEtunimetCachedNotNull() > 0L) {
            log.info("Henkilötietojen olemassa olevan cachen päivitys alkaa");
            ScheduleTimestamps scheduleTimestamps = this.scheduleTimestampsDataRepository.findFirstByIdentifier("henkilocache")
                    .orElseThrow(DataInconsistencyException::new);
            List<String> modifiedOidHenkiloList = new ArrayList<>();
            long amount = 1000L;
            for(long offset = 0; offset == 0 || !modifiedOidHenkiloList.isEmpty() || !(modifiedOidHenkiloList.size() < amount); offset++) {
                modifiedOidHenkiloList = this.oppijanumerorekisteriClient
                        .getModifiedSince(scheduleTimestamps.getModified(),offset*amount, amount);
                if (!modifiedOidHenkiloList.isEmpty()) {
                    this.henkiloCacheService.saveAll(0, amount, modifiedOidHenkiloList);
                }
            }
            scheduleTimestamps.setModified(LocalDateTime.now());
        }
        // Fetch whole henkilo nimi cache
        else {
            log.info("Henkilötietojen uuden cachen luominen alkaa");
            Long count = 1000L;
            for(long page = 0; !this.henkiloCacheService.saveAll(page*count, count, null); page++) {
                // Escape condition in case of inifine loop (10M+ henkilos)
                if (page > 10000) {
                    LOG.error("Infinite loop detected with page "+ page + " and count " + count + ". Henkilo cache might not be fully updated!");
                    break;
                }
            }
            this.scheduleTimestampsDataRepository.findFirstByIdentifier("henkilocache")
                    .orElseThrow(DataInconsistencyException::new)
                    .setModified(LocalDateTime.now());
        }
        log.info("Henkilötietojen cachen päivitys päättyy");
    }
}
