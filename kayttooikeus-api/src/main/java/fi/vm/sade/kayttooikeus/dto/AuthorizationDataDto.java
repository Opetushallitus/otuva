package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorizationDataDto {
    private AccessRightListTypeDto accessrights;
    private GroupListTypeDto groups;
}
