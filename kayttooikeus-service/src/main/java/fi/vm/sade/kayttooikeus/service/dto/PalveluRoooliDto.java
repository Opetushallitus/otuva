package fi.vm.sade.kayttooikeus.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PalveluRoooliDto {
    private String palveluName;
    private List<LocaleTextDto> palveluTexts = new ArrayList<>();
    private String rooli;
    private List<LocaleTextDto> rooliTexts = new ArrayList<>();
}
