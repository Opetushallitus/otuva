package fi.vm.sade.kayttooikeus.config.scheduling;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Ajastuksen aktivointi.
 *
 * @see ScheduledTasks ajastusten konfigurointi
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "kayttooikeus.scheduling.enabled")
public class SchedulingConfiguration {

}
