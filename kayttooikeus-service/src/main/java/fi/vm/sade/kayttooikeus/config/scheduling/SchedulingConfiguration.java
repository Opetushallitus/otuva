package fi.vm.sade.kayttooikeus.config.scheduling;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Ajastuksen aktivointi.
 *
 * @see ScheduledTasks ajastusten konfigurointi
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "kayttooikeus.scheduling.enabled")
@RequiredArgsConstructor
public class SchedulingConfiguration implements SchedulingConfigurer {

    private final Environment environment;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    @Bean(destroyMethod = "shutdown")
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(environment.getProperty("kayttooikeus.scheduling.pool_size", Integer.class));
    }

}
