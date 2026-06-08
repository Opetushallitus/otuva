package fi.vm.sade.oppijanumerorekisteri.dto;

import lombok.Setter;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YhteystietoDto implements Serializable {
    private YhteystietoTyyppi yhteystietoTyyppi;
    private String yhteystietoArvo;
}
