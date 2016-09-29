package fi.vm.sade.kayttooikeus.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by autio on 29.9.2016.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "datasource", ignoreUnknownFields = false)
public class DatasourceProperties {
    private String url;
    private String user;
    private String password;
    private Integer maxActive;
    private Integer maxWait;
    private Integer maxLifetimeMillis;
}
