package fi.vm.sade.kayttooikeus.config.scheduling;

import com.github.kagkarlsson.scheduler.task.Daily;
import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.RecurringTask;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.config.properties.KayttooikeusProperties;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

@Slf4j
@Component
public class DisableInactiveServiceUsersTask extends RecurringTask {

    private final HenkiloService henkiloService;
    private final String systemUserOid;
    private final Period inactivityThreshold;

    public DisableInactiveServiceUsersTask(KayttooikeusProperties kayttooikeusProperties, CommonProperties commonProperties, HenkiloService henkiloService) {
        super("Disable inactive service users", new Daily(LocalTime.of(
                kayttooikeusProperties.getScheduling().getConfiguration().getDisableInactiveServiceUsersHour(),
                kayttooikeusProperties.getScheduling().getConfiguration().getDisableInactiveServiceUsersMinute())));
        this.henkiloService = henkiloService;
        this.inactivityThreshold = Period.parse(kayttooikeusProperties.getScheduling().getConfiguration().getDisableInactiveServiceUsersThreshold());
        this.systemUserOid = commonProperties.getAdminOid();
    }

    @Override
    public void execute(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        LocalDateTime passiveSince = LocalDateTime.now().minus(inactivityThreshold);
        summarize(passivateUnusedServiceUsers(passiveSince));
    }

    protected Map<Boolean, Integer> passivateUnusedServiceUsers(LocalDateTime passiveSince) {
        return henkiloService.findPassiveServiceUsers(passiveSince).stream()
                .map(henkilo -> {
                    try {
                        log.info("Inactive service found. Passivating {}", henkilo.getOidHenkilo());
                        henkiloService.passivoi(henkilo.getOidHenkilo(), systemUserOid);
                        return true;
                    } catch (Exception e) {
                        log.error("Error during service user passivation", e);
                        return false;
                    }
                }).collect(groupingBy(Boolean::booleanValue, summingInt(success -> 1)));
    }

    private void summarize(Map<Boolean, Integer> result) {
        if (!result.isEmpty()) {
            log.info("Passivated inactive service users. {} success, {} failures",
                    result.getOrDefault(true, 0),
                    result.getOrDefault(false, 0));
        }
    }
}
