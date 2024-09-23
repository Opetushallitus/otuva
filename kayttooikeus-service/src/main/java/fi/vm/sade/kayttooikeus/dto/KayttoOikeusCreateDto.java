package fi.vm.sade.kayttooikeus.dto;

import fi.vm.sade.kayttooikeus.dto.validate.ContainsLanguages;
import lombok.*;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KayttoOikeusCreateDto {
    @NotNull
    private String rooli;
    @NotNull @ContainsLanguages
    private TextGroupDto textGroup;
    @NotNull
    private String palveluName;
}
