package fi.vm.sade.kayttooikeus.service.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PalveluRoooliDto {
    private String palveluName;
    private List<LocaleTextDto> palveluTexts = new ArrayList<>();
    private String rooli;
    private List<LocaleTextDto> rooliTexts = new ArrayList<>();
}
