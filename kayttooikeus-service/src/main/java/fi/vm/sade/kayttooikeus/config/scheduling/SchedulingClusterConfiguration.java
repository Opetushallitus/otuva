package fi.vm.sade.kayttooikeus.config.scheduling;

import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.Daily;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;

import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.config.properties.KayttooikeusProperties;
import fi.vm.sade.kayttooikeus.config.security.OphSessionMappingStorage;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.export.ExportTask;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.service.KayttooikeusAnomusService;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import fi.vm.sade.kayttooikeus.service.MyonnettyKayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import fi.vm.sade.kayttooikeus.service.QueueingEmailService;
import fi.vm.sade.kayttooikeus.service.TaskExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.Optional;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "kayttooikeus.scheduling.enabled", matchIfMissing = true)
@RequiredArgsConstructor
public class SchedulingClusterConfiguration {
    private final KayttooikeusProperties kayttooikeusProperties;
    private final KayttooikeusAnomusService kayttooikeusAnomusService;
    private final KutsuService kutsuService;
    private final CommonProperties commonProperties;
    private final MyonnettyKayttoOikeusService myonnettyKayttoOikeusService;
    private final HenkiloDataRepository henkiloDataRepository;
    private final OrganisaatioHenkiloService organisaatioHenkiloService;
    private final QueueingEmailService queueingEmailService;
    private final TaskExecutorService taskExecutorService;
    private final OphSessionMappingStorage sessionMappingStorage;
    private final KayttajatiedotRepository kayttajatiedotRepository;
    private final UpdateHenkiloNimiCacheTask updateHenkiloNimiCacheTask;
    private final DiscardExpiredApplicationsTask discardExpiredApplicationsTask;
    private final DiscardExpiredInvitationsTask discardExpiredInvitationsTask;
    private final DisableInactiveServiceUsersTask disableInactiveServiceUsersTask;
    private final ExportTask exportTask;

    @Bean
    Task<Void> lahetaUusienAnomuksienIlmoituksetTask() {
        return Tasks
                .recurring(TaskDescriptor.of("laheta uusien anomuksien ilmoitukset task"),
                        new Daily(LocalTime.of(kayttooikeusProperties.getScheduling().getConfiguration().getKayttooikeusanomusilmoituksetHour(), 0)))
                .execute((instance, ctx) -> kayttooikeusAnomusService.lahetaUusienAnomuksienIlmoitukset(LocalDate.now().minusDays(1)));
    }

    @Bean
    Task<Void> poistaVanhentuneetKayttooikeudetTask() {
        return Tasks
                .recurring(TaskDescriptor.of("poista vanhentuneet kayttooikeudet task"),
                        new Daily(LocalTime.of(kayttooikeusProperties.getScheduling().getConfiguration().getVanhentuneetkayttooikeudetHour(), 10)))
                .execute((instance, ctx) -> {
                        Optional<Henkilo> kasittelija = henkiloDataRepository.findByOidHenkilo(commonProperties.getAdminOid());
                        myonnettyKayttoOikeusService.poistaVanhentuneet(new MyonnettyKayttoOikeusService.DeleteDetails(
                                kasittelija.get(), KayttoOikeudenTila.VANHENTUNUT, "Oikeuksien poisto, vanhentunut"));
                });
    }

    @Bean
    Task<Void> kasitteleOrganisaatioLakkautusTask() {
        return Tasks
                .recurring(TaskDescriptor.of("passivoi organisaatiohenkilöt, joiden organisaatiot on passivoitu task"),
                        new Daily(LocalTime.of(kayttooikeusProperties.getScheduling().getConfiguration().getLakkautetutOrganisaatiotHour(), 20)))
                .execute((instance, ctx) -> organisaatioHenkiloService.kasitteleOrganisaatioidenLakkautus(this.commonProperties.getAdminOid()));
    }

    @Bean
    Task<Void> sendExpirationRemindersTask() {
        return Tasks
                .recurring(TaskDescriptor.of("send expiration reminders task"),
                        new Daily(LocalTime.of(kayttooikeusProperties.getScheduling().getConfiguration().getKayttooikeusmuistutusHour(), 30)))
                .execute((instance, ctx) -> taskExecutorService.sendExpirationReminders(Period.ofWeeks(4), Period.ofWeeks(1)));
    }

    @Bean
    Task<Void> casClientSessionCleanerTask() {
        return Tasks
                .recurring(TaskDescriptor.of("cas client session cleaner"), FixedDelay.of(Duration.ofHours(1)))
                .execute((instance, ctx) -> sessionMappingStorage.clean());
    }

    @Bean
    Task<Void> updateHenkiloNimiCache() {
        return Tasks
                .recurring(TaskDescriptor.of("update henkilo nimi cache task"),
                        FixedDelay.of(Duration.ofMillis(kayttooikeusProperties.getScheduling().getConfiguration().getHenkiloNimiCache())))
                .execute((instance, ctx) -> updateHenkiloNimiCacheTask.execute());
    }

    @Bean
    Task<Void> identificationCleanupTask() {
        return Tasks
                .recurring(TaskDescriptor.of("IdentificationCleanupTask"),
                        new Daily(LocalTime.of(
                                kayttooikeusProperties.getScheduling().getConfiguration().getIdentificationCleanupHour(),
                                kayttooikeusProperties.getScheduling().getConfiguration().getIdentificationCleanupMinute())))
                .execute((instance, ctx) -> {
                        log.info("Start identification cleanup process");
                        int rows = kayttajatiedotRepository.cleanObsoletedIdentifications();
                        log.info("Removed {} mismatching rows", rows);
                });
    }

    @Bean
    Task<Void> discardExpiredInvitations() {
        return Tasks
                .recurring(TaskDescriptor.of("expire-invitations-task"),
                        new Daily(LocalTime.of(
                                kayttooikeusProperties.getScheduling().getConfiguration().getDiscardExpiredInvitationsHour(),
                                kayttooikeusProperties.getScheduling().getConfiguration().getDiscardExpiredInvitationsMinute())))
                .execute((instance, ctx) -> discardExpiredInvitationsTask.expire("invitation", kutsuService,
                        Period.ofMonths(kayttooikeusProperties.getScheduling().getConfiguration().getExpirationThreshold())));
    }

    @Bean
    Task<Void> discardExpiredApplications() {
        return Tasks
                .recurring(TaskDescriptor.of("expire-applications-task"),
                        new Daily(LocalTime.of(
                                kayttooikeusProperties.getScheduling().getConfiguration().getDiscardExpiredInvitationsHour(),
                                kayttooikeusProperties.getScheduling().getConfiguration().getDiscardExpiredInvitationsMinute())))
                .execute((instance, ctx) -> discardExpiredApplicationsTask.expire("applications", kayttooikeusAnomusService,
                        Period.ofMonths(kayttooikeusProperties.getScheduling().getConfiguration().getExpirationThreshold())));
    }

    @Bean
    Task<Void> disableInactiveServiceUsers() {
        return Tasks
                .recurring(TaskDescriptor.of("Disable inactive service users"),
                        new Daily(LocalTime.of(kayttooikeusProperties.getScheduling().getConfiguration().getDisableInactiveServiceUsersHour(), 0)))
                .execute((instance, ctx) -> disableInactiveServiceUsersTask.execute());
    }

    @Bean
    @ConditionalOnProperty(name = "kayttooikeus.tasks.export.enabled", matchIfMissing = true)
    Task<Void> exportTaskSchedule() {
        return Tasks
                .recurring(TaskDescriptor.of("ExportTask"), FixedDelay.of(Duration.ofHours(1)))
                .execute((instance, ctx) -> exportTask.execute());
    }

    @Bean
    Task<Void> emailRetryTask() {
        return Tasks
                .recurring(TaskDescriptor.of("EmailRetryTask"), FixedDelay.of(Duration.ofMinutes(5)))
                .execute((instance, ctx) -> queueingEmailService.emailRetryTask());
    }
}
