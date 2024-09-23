package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

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
