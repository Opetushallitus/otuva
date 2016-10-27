package fi.vm.sade.kayttooikeus.service.dto;


import fi.vm.sade.kayttooikeus.dto.KayttoOikeusRyhmaDto;
import fi.vm.sade.kayttooikeus.dto.PalveluDto;
import fi.vm.sade.kayttooikeus.dto.TextGroupDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class KayttoOikeusDto {
    private String rooli;
    private TextGroupDto textGroup;
    private Set<KayttoOikeusRyhmaDto> kayttoOikeusRyhmas = new HashSet<>();
    private PalveluDto palvelu;
}
