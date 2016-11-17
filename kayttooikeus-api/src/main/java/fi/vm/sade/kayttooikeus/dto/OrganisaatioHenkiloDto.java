package fi.vm.sade.kayttooikeus.dto;

import lombok.*;
import org.joda.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganisaatioHenkiloDto {
    private Long id;
    private String organisaatioOid;
    private OrganisaatioHenkiloTyyppi organisaatioHenkiloTyyppi;
    private String tehtavanimike;
    private boolean passivoitu;
    private LocalDate voimassaAlkuPvm;
    private LocalDate voimassaLoppuPvm;
}
