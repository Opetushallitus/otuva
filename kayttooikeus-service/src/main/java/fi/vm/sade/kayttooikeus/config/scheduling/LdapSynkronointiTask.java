package fi.vm.sade.kayttooikeus.config.scheduling;

import com.github.kagkarlsson.scheduler.task.*;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.config.properties.KayttooikeusProperties;
import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService;
import fi.vm.sade.kayttooikeus.service.MyonnettyKayttoOikeusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;

/**
 *
 * @see SchedulingClusterConfiguration ajastuksen aktivointi
 */
@Slf4j
@Component
public class LdapSynkronointiTask extends RecurringTask {
    private final LdapSynchronizationService ldapSynchronizationService;
    @Autowired
    public LdapSynkronointiTask(KayttooikeusProperties kayttooikeusProperties,
                                LdapSynchronizationService ldapSynchronizationService) {
        super("ldap synkronointi task",
                FixedDelay.of(Duration.ofMillis(kayttooikeusProperties.getScheduling().getLdapsynkronointi().getFixeddelayinmillis())));
        this.ldapSynchronizationService = ldapSynchronizationService;
    }

    @Override
    public void execute(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        this.ldapSynchronizationService.runSynchronizer();
    }
}
