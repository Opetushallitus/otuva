package fi.vm.sade.kayttooikeus.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AccessRightTypeDto {
    private String organisaatioOid;
    private String palvelu;
    private String rooli;
}
