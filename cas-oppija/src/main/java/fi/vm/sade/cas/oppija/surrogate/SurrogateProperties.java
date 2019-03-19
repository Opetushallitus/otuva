package fi.vm.sade.cas.oppija.surrogate;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Configuration
@ConfigurationProperties(prefix = "valtuudet")
public class SurrogateProperties {

    private final static String HMAC_ALGORITHM = "HmacSHA256";

    private String host;
    private String clientId;
    private String apiKey;
    private String oauthPassword;
    private Duration sessionTimeout;

    public SurrogateProperties() {
    }

    public static SurrogateProperties ofCredentials(String clientId, String apiKey, String oauthPassword) {
        SurrogateProperties properties = new SurrogateProperties();
        properties.clientId = clientId;
        properties.apiKey = apiKey;
        properties.oauthPassword = oauthPassword;
        return properties;
    }

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

    public Duration getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Duration sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public String getCredentials() {
        String credentials = clientId + ":" + oauthPassword;
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    public String getChecksum(String path, Instant instant) {
        String timestamp = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
        return clientId + " " + timestamp + " " + hash(path + " " + timestamp, apiKey);
    }

    private String hash(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return new String(Base64.getEncoder().encode(mac.doFinal(data.getBytes(StandardCharsets.UTF_8))));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new UnsupportedOperationException(e);
        }
    }

}
