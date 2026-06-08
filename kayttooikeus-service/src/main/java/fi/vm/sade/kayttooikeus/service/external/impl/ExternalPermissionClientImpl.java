package fi.vm.sade.kayttooikeus.service.external.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.kayttooikeus.config.HttpClientConfiguration;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckRequestDto;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckResponseDto;
import fi.vm.sade.kayttooikeus.service.external.ExternalPermissionClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class ExternalPermissionClientImpl implements ExternalPermissionClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    public final Map<ExternalPermissionService, String> SERVICE_URIS = new HashMap<>();

    @Value("${url-virkailija}")
    private String urlVirkailija;
    @Value("${url-varda}")
    private String urlVarda;

    public ExternalPermissionClientImpl(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;

        SERVICE_URIS.put(ExternalPermissionService.HAKU_APP, "/haku-app/permission/checkpermission");
        SERVICE_URIS.put(ExternalPermissionService.SURE, "/suoritusrekisteri/permission/checkpermission");
        SERVICE_URIS.put(ExternalPermissionService.ATARU, "/lomake-editori/api/checkpermission");
        SERVICE_URIS.put(ExternalPermissionService.KOSKI, "/koski/api/permission/checkpermission");
        SERVICE_URIS.put(ExternalPermissionService.VARDA, "/varda/api/onr/external-permissions/");
    }

    @Override
    public PermissionCheckResponseDto getPermission(ExternalPermissionService service, PermissionCheckRequestDto requestDto) {
        try {
            String path = requireNonNull(SERVICE_URIS.get(service), "service uri puuttuu: " + service);
            String host = ExternalPermissionService.VARDA.equals(service) ? urlVarda : urlVirkailija;
            String body = objectMapper.writeValueAsString(requestDto);
            var request = HttpRequest.newBuilder()
                    .uri(new URI(host + path))
                    .header("Caller-Id", HttpClientConfiguration.CALLER_ID)
                    .header("CSRF", "CSRF")
                    .header("Cookie", "CSRF=CSRF")
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(BodyPublishers.ofString(body))
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), PermissionCheckResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
