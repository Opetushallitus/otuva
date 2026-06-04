package fi.vm.sade.kayttooikeus.config;

import java.net.CookieManager;
import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfiguration {
    public static final String CALLER_ID = "1.2.246.562.10.00000000001.kayttooikeus-service.backend";

    @Bean
    HttpClient client() {
        return HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
}
