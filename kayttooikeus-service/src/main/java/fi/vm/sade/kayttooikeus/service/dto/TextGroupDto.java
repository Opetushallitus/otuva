package fi.vm.sade.kayttooikeus.service.dto;

import fi.vm.sade.kayttooikeus.dto.TextDto;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
public class TextGroupDto {
    private Long id;
    private Set<TextDto> texts = new HashSet<TextDto>();
}
