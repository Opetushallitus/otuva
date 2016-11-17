package fi.vm.sade.kayttooikeus.dto.permissioncheck;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionCheckResponseDto {

    private boolean accessAllowed = false;
    private String errorMessage;

    public boolean isAccessAllowed() {
        return accessAllowed;
    }

}
