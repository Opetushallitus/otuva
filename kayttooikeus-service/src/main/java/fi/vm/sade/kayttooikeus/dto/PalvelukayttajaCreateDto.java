package fi.vm.sade.kayttooikeus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PalvelukayttajaCreateDto {

    @NotNull
    private String nimi;

}
