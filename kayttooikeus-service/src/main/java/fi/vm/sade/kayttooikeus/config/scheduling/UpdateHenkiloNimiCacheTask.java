package fi.vm.sade.kayttooikeus.config.scheduling;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.FixedDelay;
import com.github.kagkarlsson.scheduler.task.RecurringTask;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import fi.vm.sade.kayttooikeus.config.properties.KayttooikeusProperties;
import fi.vm.sade.kayttooikeus.model.ScheduleTimestamps;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.ScheduleTimestampsDataRepository;
import fi.vm.sade.kayttooikeus.service.HenkiloCacheService;
import fi.vm.sade.kayttooikeus.service.exception.DataInconsistencyException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @see SchedulingClusterConfiguration ajastuksen aktivointi
 */
@Slf4j
@Component
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class UpdateHenkiloNimiCacheTask extends RecurringTask {
    private final HenkiloDataRepository henkiloDataRepository;
    private final HenkiloCacheService henkiloCacheService;
    private final ScheduleTimestampsDataRepository scheduleTimestampsDataRepository;
    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @Autowired
    public UpdateHenkiloNimiCacheTask(KayttooikeusProperties kayttooikeusProperties,
                                      HenkiloDataRepository henkiloDataRepository,
                                      HenkiloCacheService henkiloCacheService,
                                      ScheduleTimestampsDataRepository scheduleTimestampsDataRepository,
                                      OppijanumerorekisteriClient oppijanumerorekisteriClient) {
        super("update henkilo nimi cache task",
                FixedDelay.of(Duration.ofMillis(kayttooikeusProperties.getScheduling().getConfiguration().getHenkiloNimiCache())));
        this.henkiloDataRepository = henkiloDataRepository;
        this.henkiloCacheService = henkiloCacheService;
        this.scheduleTimestampsDataRepository = scheduleTimestampsDataRepository;
        this.oppijanumerorekisteriClient = oppijanumerorekisteriClient;
    }

    @Override
    @Transactional
    public void execute(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        // Update existing cache
        if (this.henkiloDataRepository.countByEtunimetCachedNotNull() > 0L) {
            updateExistingHenkiloCache();
        }
        // Fetch whole henkilo nimi cache
        else {
            populateNewHenkiloCache();
        }
        log.info("Henkilötietojen cachen päivitys päättyy");
    }

    private void populateNewHenkiloCache() {
        log.info("Henkilötietojen uuden cachen luominen alkaa");
        Long count = 1000L;
        for (long page = 0; !this.henkiloCacheService.saveAll(page*count, count, null); page++) {
            // Escape condition in case of inifine loop (100M+ henkilos)
            if (page > 100000) {
                log.error("Infinite loop detected with page "+ page + " and count " + count + ". Henkilo cache might not be fully updated!");
                break;
            }
        }
        this.scheduleTimestampsDataRepository.findFirstByIdentifier("henkilocache")
                .orElseThrow(DataInconsistencyException::new)
                .setModified(LocalDateTime.now());
    }

    private void updateExistingHenkiloCache() {
        log.info("Henkilötietojen olemassa olevan cachen päivitys alkaa");
        ScheduleTimestamps scheduleTimestamps = this.scheduleTimestampsDataRepository.findFirstByIdentifier("henkilocache")
                .orElseThrow(DataInconsistencyException::new);
        LocalDateTime now = LocalDateTime.now();
        List<String> modifiedOidHenkiloList = new ArrayList<>();
        long amount = 1000L;
        for (long offset = 0; offset == 0 || !modifiedOidHenkiloList.isEmpty() || !(modifiedOidHenkiloList.size() < amount); offset++) {
            modifiedOidHenkiloList = this.oppijanumerorekisteriClient
                    .getModifiedSince(scheduleTimestamps.getModified(),offset*amount, amount);
            if (!modifiedOidHenkiloList.isEmpty()) {
                // Offset 0 because modifiedOidHenkiloList.size <= amount
                this.henkiloCacheService.saveAll(0, amount, modifiedOidHenkiloList);
            }
        }
        scheduleTimestamps.setModified(now);
    }
}
