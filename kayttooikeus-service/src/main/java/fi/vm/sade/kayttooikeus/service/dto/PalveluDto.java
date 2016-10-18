package fi.vm.sade.kayttooikeus.service.dto;

import fi.vm.sade.kayttooikeus.model.PalveluTyyppi;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PalveluDto {
    private Long id;
    private String name;
    private PalveluTyyppi palveluTyyppi;
    private TextGroupDto description;
    private PalveluDto kokoelma;
}
