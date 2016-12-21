package fi.vm.sade.kayttooikeus.config.properties;


import lombok.Getter;
import lombok.Setter;
import org.joda.time.Period;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "auth", ignoreUnknownFields = false)
public class AuthProperties {
    private Period expirationMonths;

    public void setExpirationMonths(String period){
        this.expirationMonths = Period.parse(period);
    }
}
