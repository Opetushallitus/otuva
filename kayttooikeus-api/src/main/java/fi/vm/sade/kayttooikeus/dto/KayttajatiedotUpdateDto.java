package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class KayttajatiedotUpdateDto {

    @NotNull
    private String username;

    public KayttajatiedotUpdateDto() {}

    public KayttajatiedotUpdateDto(String username) {
        this.username = username;
    }
}
