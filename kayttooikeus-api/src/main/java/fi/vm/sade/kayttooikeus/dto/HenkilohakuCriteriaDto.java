package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HenkilohakuCriteriaDto {

    private Boolean subOrganisation;
    private Boolean noOrganisation;
    private Boolean passivoitu;
    private Boolean duplikaatti;

    private String nameQuery;

    private List<String> organisaatioOids;
    private Long kayttooikeusryhmaId;

}
