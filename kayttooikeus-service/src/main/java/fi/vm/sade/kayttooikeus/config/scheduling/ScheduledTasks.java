package fi.vm.sade.kayttooikeus.config.scheduling;

import fi.vm.sade.kayttooikeus.service.OrganisaatioService;
import lombok.RequiredArgsConstructor;
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

    @Scheduled(cron = "${kayttooikeus.scheduling.configuration.organisaatiocache}")
    public void updateOrganisaatioCache() {
        organisaatioService.updateOrganisaatioCache();
    }

}
