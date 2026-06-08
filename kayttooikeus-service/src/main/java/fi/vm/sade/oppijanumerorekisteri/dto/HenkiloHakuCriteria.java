package fi.vm.sade.oppijanumerorekisteri.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString
public class HenkiloHakuCriteria {
    private Set<String> henkiloOids;
    private String hetu;
    private Boolean passivoitu;
    private Boolean duplikaatti;
    private Set<String> organisaatioOids;
    private Set<String> kayttoOikeusRyhmaNimet;

}
