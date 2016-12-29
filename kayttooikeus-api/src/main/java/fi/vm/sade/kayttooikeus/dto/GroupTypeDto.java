package fi.vm.sade.kayttooikeus.dto;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupTypeDto {
    private String organisaatioOid;
    private String nimi;
}
