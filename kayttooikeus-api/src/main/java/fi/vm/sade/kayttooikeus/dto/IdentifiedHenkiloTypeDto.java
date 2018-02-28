package fi.vm.sade.kayttooikeus.dto;

import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentifiedHenkiloTypeDto {
    private Long id;
    private String oidHenkilo;
    private KayttajaTyyppi henkiloTyyppi;
    private long version;
    private boolean passivoitu;
    private String idpEntityId;
    private String identifier;
    private String kutsumanimi;
    private String etunimet;
    private String sukunimi;
    private String hetu;
    private String sukupuoli;
    private String email;
    private KayttajatiedotReadDto kayttajatiedot;
    private AuthorizationDataDto authorizationData;
    private AsiointikieliDto asiointiKieli;
}
