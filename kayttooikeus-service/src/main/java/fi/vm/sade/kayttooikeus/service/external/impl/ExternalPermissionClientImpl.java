package fi.vm.sade.kayttooikeus.service.external.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckRequestDto;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckResponseDto;
import fi.vm.sade.kayttooikeus.service.external.ExternalPermissionClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static fi.vm.sade.javautils.httpclient.OphHttpClient.JSON;
import static fi.vm.sade.javautils.httpclient.OphHttpClient.UTF8;
import static java.util.Objects.requireNonNull;

@Component
public class ExternalPermissionClientImpl implements ExternalPermissionClient {

    private final OphHttpClient httpClient;
    private final ObjectMapper objectMapper;
    public final Map<ExternalPermissionService, String> SERVICE_URIS = new HashMap<>();

    @Value("${url-virkailija}")
    private String urlVirkailija;
    @Value("${url-varda}")
    private String urlVarda;

    public ExternalPermissionClientImpl(OphHttpClient httpClient, ObjectMapper objectMapper) {
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
        String path = requireNonNull(SERVICE_URIS.get(service), "service uri puuttuu: " + service);
        String host = ExternalPermissionService.VARDA.equals(service) ? urlVarda : urlVirkailija;
        return httpClient.post(host + path)
                .dataWriter(JSON, UTF8, out -> out.write(objectMapper.writeValueAsString(requestDto)))
                .execute(response -> objectMapper.readValue(response.asInputStream(), PermissionCheckResponseDto.class));
    }

}
