package fi.vm.sade.oppijanumerorekisteri.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HenkiloUpdateDto {
    private String oidHenkilo;

    private Boolean passivoitu;

    private String etunimet;

    private String kutsumanimi;

    private String sukunimi;

    private String hetu;

    private LocalDate syntymaaika;

    private LocalDate kuolinpaiva;

    private String sukupuoli;

    private String kotikunta;

    private KielisyysDto asiointiKieli;

    private KielisyysDto aidinkieli;

    private Set<KansalaisuusDto> kansalaisuus = null;

    private Set<YhteystiedotRyhmaDto> yhteystiedotRyhma = null;
}
