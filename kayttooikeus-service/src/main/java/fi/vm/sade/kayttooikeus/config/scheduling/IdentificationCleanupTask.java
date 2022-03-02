package fi.vm.sade.kayttooikeus.config.scheduling;

import com.github.kagkarlsson.scheduler.task.Daily;
import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.RecurringTask;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import fi.vm.sade.kayttooikeus.config.properties.KayttooikeusProperties;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Slf4j
@Component
public class IdentificationCleanupTask extends RecurringTask {

    KayttajatiedotRepository kayttajatiedotRepository;

    @Autowired
    public IdentificationCleanupTask(KayttooikeusProperties kayttooikeusProperties, KayttajatiedotRepository kayttajatiedotRepository) {
        super(IdentificationCleanupTask.class.getSimpleName(),
                new Daily(LocalTime.of(
                        kayttooikeusProperties.getScheduling().getConfiguration().getIdentificationCleanupHour(),
                        kayttooikeusProperties.getScheduling().getConfiguration().getIdentificationCleanupMinute())));
        this.kayttajatiedotRepository = kayttajatiedotRepository;
    }

    @Override
    public void execute(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        log.info("Purging mismatching identifications");
        kayttajatiedotRepository.cleanObsoletedIdentifications();
    }
}
