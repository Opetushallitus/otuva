package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PalvelukayttajaCriteriaDto {
    private Boolean subOrganisation;
    private String nameQuery;
    private String organisaatioOid;
}
