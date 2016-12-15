package fi.vm.sade.kayttooikeus.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KayttajatiedotCreateDto {

    @NotNull
    @Pattern(regexp = Constants.USERNAME_REGEXP)
    private String username;

}
