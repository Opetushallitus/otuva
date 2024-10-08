package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

import jakarta.validation.constraints.NotNull;
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

    private Set<@NotNull String> organisaatioOids;
    private Long kayttooikeusryhmaId;

}
