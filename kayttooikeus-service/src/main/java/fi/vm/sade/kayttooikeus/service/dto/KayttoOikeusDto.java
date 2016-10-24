package fi.vm.sade.kayttooikeus.service.dto;


import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class KayttoOikeusDto {
    private String rooli;
    private TextGroupDto textGroup;
    private Set<KayttoOikeusRyhmaDto> kayttoOikeusRyhmas = new HashSet<>();
    private PalveluDto palvelu;
}
