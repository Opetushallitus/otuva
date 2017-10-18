package fi.vm.sade.kayttooikeus.dto;

import fi.vm.sade.kayttooikeus.dto.validate.ContainsLanguages;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class KayttoOikeusRyhmaModifyDto {
    @NotNull @ContainsLanguages
    private TextGroupDto ryhmaName; // TODO: "nimi" (Huom! API-muutos)
    @ContainsLanguages
    private TextGroupDto kuvaus;
    @NotNull
    private List<PalveluRooliDto> palvelutRoolit;
    private List<String> organisaatioTyypit;
    private String rooliRajoite;
    private List<Long> slaveIds;
}
