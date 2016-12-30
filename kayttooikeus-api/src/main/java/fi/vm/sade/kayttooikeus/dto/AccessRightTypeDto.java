package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessRightTypeDto {
    private String organisaatioOid;
    private String palvelu;
    private String rooli;
}
