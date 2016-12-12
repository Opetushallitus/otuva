package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KayttajatiedotReadDto {

    private final Long id;
    private final String username;

    public KayttajatiedotReadDto(Long id, String username) {
        this.id = id;
        this.username = username;
    }

}
