package fi.vm.sade.oppijanumerorekisteri.dto;

import lombok.Setter;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HenkiloPerustietoDto implements Serializable {
    private String oidHenkilo;

    private List<IdentificationDto> identifications;

    private String hetu;

    private List<EidasTunnisteDto> eidasTunnisteet;

    private String etunimet;

    private String kutsumanimi;

    private String sukunimi;

    private LocalDate syntymaaika;

    private boolean turvakielto;

    private KielisyysDto aidinkieli;

    private KielisyysDto asiointiKieli;

    private Set<KansalaisuusDto> kansalaisuus;

    private String sukupuoli;

    private Date modified;
}
