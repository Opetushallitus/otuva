package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.LocalDate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class OrganisaatioHenkiloUpdateDto {
    @NotNull @Size(min = 1)
    private String organisaatioOid;
    private OrganisaatioHenkiloTyyppi organisaatioHenkiloTyyppi;
    private LocalDate voimassaAlkuPvm;
    private LocalDate voimassaLoppuPvm;
    private String tehtavanimike;
    private boolean passivoitu;
}
