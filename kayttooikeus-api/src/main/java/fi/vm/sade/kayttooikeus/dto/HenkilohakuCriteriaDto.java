package fi.vm.sade.kayttooikeus.dto;

import fi.vm.sade.kayttooikeus.dto.validate.NoNullElements;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HenkilohakuCriteriaDto {

    private Boolean subOrganisation;
    private Boolean noOrganisation;
    private Boolean passivoitu;
    private Boolean duplikaatti;
    private String sukunimi;
    private String kayttajatunnus;
    private KayttajaTyyppi kayttajaTyyppi;

    private String nameQuery;

    @NoNullElements
    private Set<String> organisaatioOids;
    private Long kayttooikeusryhmaId;

}
