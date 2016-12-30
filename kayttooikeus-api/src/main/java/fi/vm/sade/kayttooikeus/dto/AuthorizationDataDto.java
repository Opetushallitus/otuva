package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthorizationDataDto {
    private AccessRightListTypeDto accessrights;
    private GroupListTypeDto groups;
}
