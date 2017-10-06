package fi.vm.sade.kayttooikeus.config.scheduling;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.ExternalServiceException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Ajastuksen aktivointi.
 *
 * @see ScheduledTasks ajastusten konfigurointi
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulingConfiguration implements SchedulingConfigurer {

    private final Environment environment;

    private final OrganisaatioClient organisaatioClient;

    private ScheduledFuture organisaatioRetryTask;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
        if(BooleanUtils.isTrue(this.environment.getProperty("kayttooikeus.scheduling.run-on-startup", Boolean.class))) {
            this.onStartup(taskRegistrar.getScheduler());
        }
    }

    private void onStartup(TaskScheduler taskScheduler) {
        this.organisaatioRetryTask = taskScheduler.scheduleWithFixedDelay(() -> {
            this.organisaatioClient.refreshCache();
            this.organisaatioRetryTask.cancel(false);
        }, this.environment.getProperty("kayttooikeus.scheduling.organisaatio-retry-time", Long.class));
    }

    @Bean(destroyMethod = "shutdown")
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(environment.getProperty("kayttooikeus.scheduling.pool_size", Integer.class));
    }

}
