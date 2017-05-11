package fi.vm.sade.kayttooikeus.config.scheduling;

import fi.vm.sade.kayttooikeus.service.OrganisaatioService;
import fi.vm.sade.kayttooikeus.service.TaskExecutorService;
import lombok.RequiredArgsConstructor;
import org.joda.time.Period;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Ajastusten konfigurointi.
 *
 * @see SchedulingConfiguration ajastuksen aktivointi
 */
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final OrganisaatioService organisaatioService;
    private final TaskExecutorService taskExecutorService;

    @Scheduled(cron = "${kayttooikeus.scheduling.configuration.organisaatiocache}")
    public void updateOrganisaatioCache() {
        organisaatioService.updateOrganisaatioCache();
    }

    @Scheduled(cron = "${kayttooikeus.scheduling.configuration.kayttooikeusmuistutus}")
    public void sendExpirationReminders() {
        taskExecutorService.sendExpirationReminders(Period.weeks(4), Period.weeks(1));
    }

}
