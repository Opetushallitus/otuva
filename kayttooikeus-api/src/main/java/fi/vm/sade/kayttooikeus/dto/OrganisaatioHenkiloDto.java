package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.LocalDate;

@Getter
@Setter
public class OrganisaatioHenkiloDto {
    private Long id;
    private String organisaatioOid;
    private OrganisaatioHenkiloTyyppi organisaatioHenkiloTyyppi;
    private String tehtavanimike;
    private boolean passivoitu;
    private LocalDate voimassaAlkuPvm;
    private LocalDate voimassaLoppuPvm;
}
