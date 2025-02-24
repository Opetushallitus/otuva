package fi.vm.sade.kayttooikeus.service.external;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckRequestDto;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckResponseDto;
import fi.vm.sade.kayttooikeus.service.external.impl.ExternalPermissionClientImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class ExternalPermissionClientTest extends AbstractClientTest {
    @Autowired
    private ExternalPermissionClient client;

    @Test
    public void getPermission() {
        patchWireMockHostToClientConfig();

        stubFor(post(anyUrl())
                .withRequestBody(equalToJson("{\"personOidsForSamePerson\":null,\"organisationOids\":[],\"loggedInUserRoles\":null,\"loggedInUserOid\":\"1.2.2.1\"}"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody("{}")));

        stubFor(post(anyUrl())
                .withRequestBody(equalToJson("{\"personOidsForSamePerson\":null,\"organisationOids\":[],\"loggedInUserRoles\":null,\"loggedInUserOid\":\"1.2.2.2\"}"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody("{\"accessAllowed\":true,\"errorMessage\":null}")));

        Arrays.stream(ExternalPermissionService.values()).forEach(this::getPermission);
    }

    private void getPermission(ExternalPermissionService service) {
        PermissionCheckRequestDto dto = new PermissionCheckRequestDto();
        if (service == ExternalPermissionService.HAKU_APP) {
            dto.setLoggedInUserOid("1.2.2.2");
            PermissionCheckResponseDto response = client.getPermission(service, dto);

            assertThat(response).returns(true, PermissionCheckResponseDto::isAccessAllowed);
        } else {
            dto.setLoggedInUserOid("1.2.2.1");
            PermissionCheckResponseDto response = client.getPermission(service, dto);

            assertThat(response).returns(false, PermissionCheckResponseDto::isAccessAllowed);
        }
    }

    void patchWireMockHostToClientConfig() {
        if (client instanceof ExternalPermissionClientImpl c) {
            c.SERVICE_URIS.forEach((service, uri) ->
                c.SERVICE_URIS.put(service, uri.replaceFirst("https://localhost", WIREMOCK_HOST)));
        }
    }

}