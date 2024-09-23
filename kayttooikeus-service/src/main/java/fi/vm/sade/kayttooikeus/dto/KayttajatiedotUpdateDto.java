package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
public class KayttajatiedotUpdateDto {

    @NotNull
    @Pattern(regexp = Constants.USERNAME_REGEXP)
    private String username;

    public KayttajatiedotUpdateDto() {}

    public KayttajatiedotUpdateDto(String username) {
        this.username = username;
    }
}
