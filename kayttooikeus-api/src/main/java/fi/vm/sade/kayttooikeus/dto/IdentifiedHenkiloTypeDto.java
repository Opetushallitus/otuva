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
    private HenkiloTyyppi henkiloTyyppi;
    private long version;
    private boolean passivoitu;
    private String idpEntityId;
    private String identifier;
    private KayttajatiedotReadDto kayttajatiedot;
    private AuthorizationDataDto authorizationData;
}
