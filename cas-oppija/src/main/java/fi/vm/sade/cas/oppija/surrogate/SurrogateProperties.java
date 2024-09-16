package fi.vm.sade.cas.oppija.surrogate;

import fi.vm.sade.suomifi.valtuudet.ValtuudetProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "valtuudet")
public class SurrogateProperties implements ValtuudetProperties {

    private String host;
    private String clientId;
    private String apiKey;
    private String oauthPassword;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getOauthPassword() {
        return oauthPassword;
    }

    public void setOauthPassword(String oauthPassword) {
        this.oauthPassword = oauthPassword;
    }

}
