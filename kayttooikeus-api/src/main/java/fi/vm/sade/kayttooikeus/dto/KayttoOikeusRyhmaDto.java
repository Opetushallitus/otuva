package fi.vm.sade.kayttooikeus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class KayttoOikeusRyhmaDto {
    private Long id;
    private String name;
    private String rooliRajoite;
    private List<OrganisaatioViiteDto> organisaatioViite = new ArrayList<>();
    private TextGroupDto description;
}
