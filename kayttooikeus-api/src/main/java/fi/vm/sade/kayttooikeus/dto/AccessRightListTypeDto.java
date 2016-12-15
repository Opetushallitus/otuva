package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccessRightListTypeDto {
    protected List<AccessRightTypeDto> accessRight;
}
