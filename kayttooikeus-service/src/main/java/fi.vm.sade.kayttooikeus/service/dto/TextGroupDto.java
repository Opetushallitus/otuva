package fi.vm.sade.kayttooikeus.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by autio on 4.10.2016.
 */
@Getter
@Setter
public class TextGroupDto {
    private Long id;
    private Set<TextDto> texts = new HashSet<TextDto>();
}
