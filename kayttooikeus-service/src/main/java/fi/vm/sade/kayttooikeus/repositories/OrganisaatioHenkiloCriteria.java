package fi.vm.sade.kayttooikeus.repositories;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrganisaatioHenkiloCriteria {

    private Boolean passivoitu;
    private String organisaatioOid;

}
