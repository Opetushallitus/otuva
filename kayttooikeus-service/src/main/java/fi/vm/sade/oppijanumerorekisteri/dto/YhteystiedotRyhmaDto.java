package fi.vm.sade.oppijanumerorekisteri.dto;

import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YhteystiedotRyhmaDto implements Serializable {
    private Long id;

    private String ryhmaKuvaus;

    private String ryhmaAlkuperaTieto;

    private boolean readOnly;

    @Builder.Default
    private Set<YhteystietoDto> yhteystieto = new HashSet<>();

}
