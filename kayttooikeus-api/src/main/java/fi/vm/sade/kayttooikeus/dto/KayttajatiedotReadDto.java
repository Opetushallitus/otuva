package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KayttajatiedotReadDto {

    private final String username;
    private final MfaProvider mfaProvider;

    public KayttajatiedotReadDto(String username, MfaProvider mfaProvider) {
        this.username = username;
        this.mfaProvider = mfaProvider;
    }

}
