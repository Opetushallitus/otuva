package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
public class HenkilohakuCriteriaDto {

    private Boolean subOrganisation;
    private Boolean noOrganisation;
    private Boolean passivoitu;
    private Boolean duplikaatti;

    private String nameQuery;

    private String organisaatioOid;
    private Long kayttooikeusryhmaId;

}
