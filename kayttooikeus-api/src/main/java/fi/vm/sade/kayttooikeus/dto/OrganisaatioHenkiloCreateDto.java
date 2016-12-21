package fi.vm.sade.kayttooikeus.dto;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.LocalDate;

@Getter
@Setter
public class OrganisaatioHenkiloCreateDto {

    @NotNull
    private String organisaatioOid;
    private OrganisaatioHenkiloTyyppi organisaatioHenkiloTyyppi;
    private LocalDate voimassaAlkuPvm;
    private LocalDate voimassaLoppuPvm;
    private String tehtavanimike;

}
