package fi.vm.sade.kayttooikeus.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by autio on 4.10.2016.
 */
@Getter
@Setter
public class KayttoOikeusRyhmaDto {
    private Long id;
    private String name;
    private String rooliRajoite;
    private List<OrganisaatioViiteDto> organisaatioViite = new ArrayList<>();
    private TextGroupDto description;
}
