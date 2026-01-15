package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganisaatioHenkiloDto implements Serializable {
    private long id;
    private String organisaatioOid;
    private boolean passivoitu;
}
