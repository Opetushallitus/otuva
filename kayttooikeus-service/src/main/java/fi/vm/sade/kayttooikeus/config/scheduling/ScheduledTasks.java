package fi.vm.sade.kayttooikeus.config.scheduling;

import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.service.KayttooikeusAnomusService;
import fi.vm.sade.kayttooikeus.service.MyonnettyKayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.OrganisaatioService;
import fi.vm.sade.kayttooikeus.service.TaskExecutorService;
import lombok.RequiredArgsConstructor;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService;

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
    private final CommonProperties commonProperties;
    private final TaskExecutorService taskExecutorService;
    private final KayttooikeusAnomusService kayttooikeusAnomusService;
    private final LdapSynchronizationService ldapSynchronizationService;

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
        taskExecutorService.sendExpirationReminders(Period.weeks(4), Period.weeks(1));
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

}
