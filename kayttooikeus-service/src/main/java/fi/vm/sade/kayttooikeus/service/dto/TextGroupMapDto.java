package fi.vm.sade.kayttooikeus.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import fi.vm.sade.kayttooikeus.model.Text;
import fi.vm.sade.kayttooikeus.model.TextGroup;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class TextGroupMapDto implements Serializable, Localizable {
    @Getter
    private final Long id;
    @Getter
    private final Map<String, String> texts;

    @JsonCreator
    public TextGroupMapDto(Long id, Map<String, String> values) {
        this.id = id;
        this.texts = values == null ? null : new HashMap<>(values);
    }

    @JsonValue
    public Map<String, String> asMap() {
        return texts;
    }
    
    @JsonIgnore
    public String get(String lang) {
        return texts.get(lang);
    }

    @Override
    public void put(String lang, String value) {
        this.texts.put(lang, value);
    }

    public static TextGroupMapDto localizeAsMapLaterById(Long id) {
        return id == null ? null : new TextGroupMapDto(id, new HashMap<>());
    }

    @SuppressWarnings("DtoClassesNotContainEntities")
    public static TextGroupMapDto localized(TextGroup grp) {
        return grp == null ? null : new TextGroupMapDto(grp.getId(), grp.getTexts().stream()
                .collect(toMap(Text::getLang, Text::getText)));
    }
}
