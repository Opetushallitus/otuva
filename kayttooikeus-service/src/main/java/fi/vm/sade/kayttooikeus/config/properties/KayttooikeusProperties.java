package fi.vm.sade.kayttooikeus.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kayttooikeus")
public class KayttooikeusProperties {
    private Scheduling scheduling = new Scheduling();

    @Getter @Setter
    public static class Scheduling {
        private Boolean enabled = false;
        private Boolean runOnStartup = true;
        private Integer pool_size = 2;
        private Long organisaatioRetryTime = 300000L;

        private Configuration configuration = new Configuration();

        @Getter @Setter
        public static class Configuration {
            private Long organisaatiocache; // Default in ScheduledTasks.java
            private Integer vanhentuneetkayttooikeudetHour = 3;
            private Integer lakkautetutOrganisaatiotHour = 5;
            private Integer kayttooikeusmuistutusHour = 4;
            private Integer kayttooikeusmuistutusMinute = 30;
            private Integer kayttooikeusanomusilmoituksetHour = 2;
            private Integer purgeExpiredInvitationsHour = 1;
            private Integer purgeExpiredInvitationsMinute = 30;
            private Long henkiloNimiCache = 100000L;
        }

    }
}
