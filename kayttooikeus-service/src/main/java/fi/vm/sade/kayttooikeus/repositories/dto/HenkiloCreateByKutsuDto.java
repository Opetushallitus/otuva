package fi.vm.sade.kayttooikeus.repositories.dto;

import fi.vm.sade.kayttooikeus.dto.Constants;
import fi.vm.sade.oppijanumerorekisteri.dto.KielisyysDto;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class HenkiloCreateByKutsuDto {
    @NotNull
    @Min(1)
    private String kutsumanimi;

    private KielisyysDto asiointiKieli;

    @NotNull
    @Min(1)
    @Pattern(regexp = Constants.USERNAME_REGEXP)
    private String kayttajanimi;

    @NotNull
    @Min(1)
    private String password;
}
