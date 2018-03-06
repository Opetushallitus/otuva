package fi.vm.sade.kayttooikeus.config.scheduling;

import com.github.kagkarlsson.scheduler.task.Daily;
import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.RecurringTask;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import fi.vm.sade.kayttooikeus.config.properties.KayttooikeusProperties;
import fi.vm.sade.kayttooikeus.service.KayttooikeusAnomusService;
import fi.vm.sade.kayttooikeus.service.TaskExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;

/**
 *
 * @see SchedulingClusterConfiguration ajastuksen aktivointi
 */
@Slf4j
@Component
public class LahetaUusienAnomuksienIlmoituksetTask extends RecurringTask {
    private final KayttooikeusAnomusService kayttooikeusAnomusService;

    @Autowired
    public LahetaUusienAnomuksienIlmoituksetTask(KayttooikeusProperties kayttooikeusProperties,
                                                 KayttooikeusAnomusService kayttooikeusAnomusService) {
        super("laheta uusien anomuksien ilmoitukset task",
                new Daily(LocalTime.of(kayttooikeusProperties.getScheduling().getConfiguration().getKayttooikeusanomusilmoituksetHour(), 0)));
        this.kayttooikeusAnomusService = kayttooikeusAnomusService;
    }

    @Override
    public void execute(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        this.kayttooikeusAnomusService.lahetaUusienAnomuksienIlmoitukset(LocalDate.now().minusDays(1));
    }
}
