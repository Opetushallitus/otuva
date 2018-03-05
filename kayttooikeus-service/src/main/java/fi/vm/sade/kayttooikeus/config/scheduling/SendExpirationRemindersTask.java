package fi.vm.sade.kayttooikeus.config.scheduling;

import com.github.kagkarlsson.scheduler.task.Daily;
import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.RecurringTask;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.config.properties.KayttooikeusProperties;
import fi.vm.sade.kayttooikeus.service.MyonnettyKayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.TaskExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.Period;

/**
 *
 * @see SchedulingClusterConfiguration ajastuksen aktivointi
 */
@Slf4j
@Component
public class SendExpirationRemindersTask extends RecurringTask {
    private final TaskExecutorService taskExecutorService;

    @Autowired
    public SendExpirationRemindersTask(KayttooikeusProperties kayttooikeusProperties,
                                       TaskExecutorService taskExecutorService) {
        super("send expiration reminders task",
                new Daily(LocalTime.of(kayttooikeusProperties.getScheduling().getConfiguration().getKayttooikeusmuistutusHour(),
                        kayttooikeusProperties.getScheduling().getConfiguration().getKayttooikeusmuistutusMinute())));
        this.taskExecutorService = taskExecutorService;
    }

    @Override
    public void execute(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        this.taskExecutorService.sendExpirationReminders(Period.ofWeeks(4), Period.ofWeeks(1));
    }
}
