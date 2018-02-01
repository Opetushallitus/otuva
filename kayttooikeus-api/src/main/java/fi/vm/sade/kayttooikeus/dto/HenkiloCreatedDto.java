package fi.vm.sade.kayttooikeus.dto;

import fi.vm.sade.oppijanumerorekisteri.dto.YhteystiedotRyhmaDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HenkiloCreatedDto {
    private String oidHenkilo;
    private String sahkoposti;
    private Set<YhteystiedotRyhmaDto> yhteystiedotRyhma = new HashSet<>();
}
