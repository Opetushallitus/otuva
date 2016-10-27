package fi.vm.sade.kayttooikeus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OrganisaatioViiteDto {
    private Long id;
    private String organisaatioTyyppi;
}
