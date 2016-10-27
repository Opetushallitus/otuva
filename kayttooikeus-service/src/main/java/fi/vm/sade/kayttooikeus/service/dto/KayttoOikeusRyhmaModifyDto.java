package fi.vm.sade.kayttooikeus.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class KayttoOikeusRyhmaModifyDto {
    private String ryhmaNameFi;
    private String ryhmaNameSv;
    private String ryhmaNameEn;
    private List<PalveluRoooliDto> palvelutRoolit;
    private List<String> organisaatioTyypit;
    private String rooliRajoite;
    private List<Long> slaveIds;

}
