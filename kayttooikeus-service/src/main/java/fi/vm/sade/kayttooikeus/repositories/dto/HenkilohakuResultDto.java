package fi.vm.sade.kayttooikeus.repositories.dto;

import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HenkilohakuResultDto {

    String oidHenkilo;

    String nimi;

    String kayttajatunnus;

    private List<String> organisaatioNimiList = new ArrayList<>();

    public HenkilohakuResultDto(String nimi, String oidHenkilo, String kayttajatunnus) {
        this.nimi = nimi;
        this.oidHenkilo = oidHenkilo;
        this.kayttajatunnus = kayttajatunnus;
    }

}
