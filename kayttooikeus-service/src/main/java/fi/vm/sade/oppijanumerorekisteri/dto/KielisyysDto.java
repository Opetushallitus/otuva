package fi.vm.sade.oppijanumerorekisteri.dto;

import lombok.Setter;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KielisyysDto implements Serializable {
    private String kieliKoodi;

    private String kieliTyyppi;
}
