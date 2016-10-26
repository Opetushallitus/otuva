package fi.vm.sade.kayttooikeus.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        return texts.get(lang.toLowerCase());
    }

    @Override
    public Optional<String> getOrAny(String lang) {
        Optional<String> opt = Optional.ofNullable(get(lang));
        if (opt.isPresent()) {
            return opt;
        }
        return texts.values().stream().filter(t -> t != null).findFirst();
    }

    @Override
    public TextGroupMapDto put(String lang, String value) {
        this.texts.put(lang, value);
        return this;
    }

    public static TextGroupMapDto localizeAsMapLaterById(Long id) {
        return id == null ? null : new TextGroupMapDto(id, new HashMap<>());
    }
}
