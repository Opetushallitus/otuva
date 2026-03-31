package fi.vm.sade.otuva.mocksubstanceservice.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mock-substance-service")
public record OtuvaMockSubstanceServiceProperties(
       CasProperties casOppija
) {
    public record CasProperties(String serverUrl, String serviceBaseUrl) {}
}
