package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessRightTypeDto {
    private String organisaatioOid;
    private String palvelu;
    private String rooli;
}
