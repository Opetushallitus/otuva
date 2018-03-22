package fi.vm.sade.kayttooikeus.config.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties(prefix = "kayttooikeus.ldap-synchronization")
public class LdapSynchronizationProperties {

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Timed {

        private int intervalInMinutes;
        private long batchSize;
        private long loadThresholdInSeconds;

    }

    private Timed dayTime = new Timed(1, 250, 15);
    private Timed nightTime = new Timed(9, 500, 15);
    private int nightStarts = 22;
    private int nightEnds = 5;
    private int statisticsSize = 25;

    public Timed getTimedProperties(int hourOfDay) {
        return getTimedProperties(isNightTime(hourOfDay));
    }

    public Timed getTimedProperties(boolean nightTime) {
        return nightTime ? getNightTime() : getDayTime();
    }

    public boolean isNightTime(int hourOfDay) {
        return hourOfDay >= getNightStarts() || hourOfDay < getNightEnds();
    }

}
