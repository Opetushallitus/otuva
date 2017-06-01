package fi.vm.sade.kayttooikeus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HenkilohakuResultDto {

    String oidHenkilo;

    String nimi;

    String kayttajatunnus;

    private List<String> organisaatioNimiList = new ArrayList<>();

    public HenkilohakuResultDto(String nimi, String oidHenkilo) {
        this.nimi = nimi;
        this.oidHenkilo = oidHenkilo;
    }

}
