package fi.vm.sade.kayttooikeus.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocaleTextDto {
    private String text;
    private String lang;

    public LocaleTextDto(String text, String lang){
        this.text = text;
        this.lang = lang;
    }
}
