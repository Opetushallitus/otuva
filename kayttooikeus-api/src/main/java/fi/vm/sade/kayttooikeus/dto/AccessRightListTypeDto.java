package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccessRightListTypeDto {
    protected List<AccessRightTypeDto> accessRight;
}
