package fi.vm.sade.oppijanumerorekisteri.dto;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Generated
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HenkiloCreateDto implements Serializable {
    private static final long serialVersionUID = -8509596443256973893L;

    private String hetu;

    private boolean passivoitu;

    private String etunimet;

    private String kutsumanimi;

    private String sukunimi;

    private KielisyysDto aidinkieli;

    private KielisyysDto asiointiKieli;

    @Builder.Default
    private Set<KansalaisuusDto> kansalaisuus = new HashSet<>();

    private LocalDate syntymaaika;

    private String sukupuoli;

    private String kotikunta;

    private String oppijanumero;

    private Boolean turvakielto;

    private Boolean eiSuomalaistaHetua;

    private Boolean yksiloity;

    private Boolean yksiloityVTJ;

    private Boolean yksilointiYritetty;

    private Boolean duplicate;

    private Date vtjsynced;

    @Builder.Default
    private Set<YhteystiedotRyhmaDto> yhteystiedotRyhma = new HashSet<>();

    @Builder.Default
    private Set<String> passinumerot = new HashSet<>();

}
