package fi.vm.sade.kayttooikeus.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kayttooikeus.scheduling.configuration")
public class ScheduleProperties {
    private String organisaatiocache = "0 0 0 * * ?";
    private String vanhentuneetkayttooikeudet = "0 0 3 * * ?";
    private String kayttooikeusmuistutus = "0 30 4 * * ?";
    private String kayttooikeusanomusilmoitukset = "0 0 2 * * ?";
    private Long henkiloNimiCache = 60000L;
}
