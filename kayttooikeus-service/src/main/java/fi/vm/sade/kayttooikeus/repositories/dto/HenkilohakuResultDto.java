package fi.vm.sade.kayttooikeus.repositories.dto;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioMinimalDto;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "oidHenkilo")
public class HenkilohakuResultDto {

    String oidHenkilo;

    String nimi;

    String kayttajatunnus;

    private List<OrganisaatioMinimalDto> organisaatioNimiList = new ArrayList<>();

    public HenkilohakuResultDto(String nimi, String oidHenkilo, String kayttajatunnus) {
        this.nimi = nimi;
        this.oidHenkilo = oidHenkilo;
        this.kayttajatunnus = kayttajatunnus;
    }


}
