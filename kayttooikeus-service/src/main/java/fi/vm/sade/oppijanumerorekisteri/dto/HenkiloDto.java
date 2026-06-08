package fi.vm.sade.oppijanumerorekisteri.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HenkiloDto implements Serializable {
    private static final long serialVersionUID = -8509596443256973893L;

    private String oidHenkilo;

    private String hetu;

    private Set<String> kaikkiHetut;

    private boolean passivoitu;

    private String etunimet;

    private String kutsumanimi;

    private String sukunimi;

    private KielisyysDto aidinkieli;

    private KielisyysDto asiointiKieli;

    @Builder.Default
    private Set<KansalaisuusDto> kansalaisuus = new HashSet<>();

    private String kasittelijaOid;

    private LocalDate syntymaaika;

    private LocalDate kuolinpaiva;

    private String sukupuoli;

    private String kotikunta;

    private String oppijanumero;

    private Boolean turvakielto;

    private boolean eiSuomalaistaHetua;

    private boolean yksiloity;

    private boolean yksiloityVTJ;

    private boolean yksilointiYritetty;

    private boolean yksiloityEidas;

    @Builder.Default
    private List<EidasTunnisteDto> eidasTunnisteet = new ArrayList<>();

    private boolean duplicate;

    private Date created;

    private Date modified;

    private Date vtjsynced;

    @Builder.Default
    private Set<YhteystiedotRyhmaDto> yhteystiedotRyhma = new HashSet<>();

    @Builder.Default
    private Set<YksilointiVirheDto> yksilointivirheet = new HashSet<>();

    private Set<String> passinumerot;
}
