package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KayttajatiedotReadDto {

    private final String username;

    public KayttajatiedotReadDto(String username) {
        this.username = username;
    }

}
