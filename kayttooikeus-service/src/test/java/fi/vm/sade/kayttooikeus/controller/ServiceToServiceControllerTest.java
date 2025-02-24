package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckDto;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
public class ServiceToServiceControllerTest extends AbstractControllerTest {
    @MockitoBean
    private PermissionCheckerService permissionCheckerService;

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA")
    public void checkUserPermissionToUser() throws Exception {
        String postContent = "{\"callingUserOid\": \"1.2.3.4.5\"," +
                "\"userOid\": \"1.2.3.1.1\"," +
                "\"allowedPalveluRooli\": {\"OPPIJANUMEROREKISTERI\": [\"HENKILON_RU\"]}," +
                "\"externalPermissionService\": \"HAKU_APP\"," +
                "\"callingUserRoles\": [\"ROLE_APP_OPPIJANUMEROREKISTERI_REKISTERINPITAJA\"]}";
        given(this.permissionCheckerService.isAllowedToAccessPerson(any(PermissionCheckDto.class))).willReturn(true);
        this.mvc.perform(post("/s2s/canUserAccessUser").content(postContent).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().string("true"));
        ArgumentCaptor<PermissionCheckDto> captor = ArgumentCaptor.forClass(PermissionCheckDto.class);
        verify(permissionCheckerService).isAllowedToAccessPerson(captor.capture());
        PermissionCheckDto dto = captor.getValue();
        assertThat(dto).isNotNull();
        assertThat(dto.getCallingUserOid()).isEqualTo("1.2.3.4.5");
        assertThat(dto.getUserOid()).isEqualTo("1.2.3.1.1");
        assertThat(dto.getAllowedPalveluRooli()).containsEntry("OPPIJANUMEROREKISTERI", Collections.singletonList("HENKILON_RU"));
        //assertThat(dto.getAllowedPalveluRooli()).extracting("OPPIJANUMEROREKISTERI").containsExactly("HENKILON_RU");
        assertThat(dto.getExternalPermissionService()).isEqualByComparingTo(ExternalPermissionService.HAKU_APP);
        assertThat(dto.getCallingUserRoles()).containsExactly("ROLE_APP_OPPIJANUMEROREKISTERI_REKISTERINPITAJA");
    }
}
