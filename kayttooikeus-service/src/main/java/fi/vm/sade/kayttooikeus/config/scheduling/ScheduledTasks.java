package fi.vm.sade.kayttooikeus.config.scheduling;

import fi.vm.sade.kayttooikeus.service.MyonnettyKayttoOikeusService;
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
    private final MyonnettyKayttoOikeusService myonnettyKayttoOikeusService;

    @Scheduled(cron = "${kayttooikeus.scheduling.configuration.organisaatiocache}")
    public void updateOrganisaatioCache() {
        organisaatioService.updateOrganisaatioCache();
    }

    @Scheduled(cron = "${kayttooikeus.scheduling.configuration.vanhentuneetkayttooikeudet}")
    public void poistaVanhentuneetKayttoOikeudet() {
        myonnettyKayttoOikeusService.poistaVanhentuneet("1.2.246.562.24.00000000001");
    }

}
