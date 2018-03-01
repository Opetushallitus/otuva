package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsiointikieliDto {
    private String kieliKoodi;
    private String kieliTyyppi;
}
