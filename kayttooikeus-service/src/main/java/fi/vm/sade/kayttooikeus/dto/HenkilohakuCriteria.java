package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HenkilohakuCriteria {
    private Boolean subOrganisation = false;
    private String nameQuery;
    private Set<String> organisaatioOids;
    private Long kayttooikeusryhmaId;
}
