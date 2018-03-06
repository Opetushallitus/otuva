package fi.vm.sade.kayttooikeus.config.scheduling;

import com.github.kagkarlsson.scheduler.task.Daily;
import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.RecurringTask;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.config.properties.KayttooikeusProperties;
import fi.vm.sade.kayttooikeus.service.MyonnettyKayttoOikeusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 *
 * @see SchedulingClusterConfiguration ajastuksen aktivointi
 */
@Slf4j
@Component
public class PoistaVanhentuneetKayttooikeudetTask extends RecurringTask {
    private final CommonProperties commonProperties;
    private final MyonnettyKayttoOikeusService myonnettyKayttoOikeusService;

    @Autowired
    public PoistaVanhentuneetKayttooikeudetTask(KayttooikeusProperties kayttooikeusProperties,
                                                MyonnettyKayttoOikeusService myonnettyKayttoOikeusService,
                                                CommonProperties commonProperties) {
        super("poista vanhentuneet kayttooikeudet task",
                new Daily(LocalTime.of(kayttooikeusProperties.getScheduling().getConfiguration().getVanhentuneetkayttooikeudetHour(), 0)));
        this.myonnettyKayttoOikeusService = myonnettyKayttoOikeusService;
        this.commonProperties = commonProperties;
    }

    @Override
    public void execute(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
            this.myonnettyKayttoOikeusService.poistaVanhentuneet(commonProperties.getAdminOid());
    }
}
