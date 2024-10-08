package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class LoginDto {

    @NotNull
    private String username;
    @NotNull
    private String password;

}
