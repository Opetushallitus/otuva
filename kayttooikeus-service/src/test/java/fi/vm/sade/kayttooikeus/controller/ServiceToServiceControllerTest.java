package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
public class ServiceToServiceControllerTest extends AbstractControllerTest {
    @MockBean
    PermissionCheckerService permissionCheckerService;

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void checkUserPermissionToUser() throws Exception {
        String postContent = "{\"callingUserOid\": \"1.2.3.4.5\"," +
                "\"userOid\": \"1.2.3.1.1\"," +
                "\"allowedRoles\": [\"READ_WRITE\"]," +
                "\"externalPermissionService\": \"HAKU_APP\"," +
                "\"callingUserRoles\": [\"ROLE_APP_HENKILONHALLINTA_OPHREKISTERI\"]}";
        given(this.permissionCheckerService.isAllowedToAccessPerson("1.2.3.4.5", "1.2.3.1.1", Collections.singletonList("READ_WRITE"),
                ExternalPermissionService.HAKU_APP, Collections.singleton("ROLE_APP_HENKILONHALLINTA_OPHREKISTERI"))).willReturn(true);
        this.mvc.perform(post("/s2s/canUserAccessUser").content(postContent).contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andExpect(content().string("true"));
    }
}
