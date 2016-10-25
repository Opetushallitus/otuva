package fi.vm.sade.kayttooikeus.dto;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public class TextGroupListDto extends TextGroupDto {
    public TextGroupListDto() {
    }

    public TextGroupListDto(Long id) {
        super(id);
    }

    @JsonValue
    public List<TextDto> asList() {
        return getTexts();
    }

    @Override
    public TextGroupListDto put(String lang, String value) {
        super.put(lang, value);
        return this;
    }

    public static TextGroupListDto localizeAsListLaterById(Long id) {
        return id == null ? null : new TextGroupListDto(id);
    }
}
