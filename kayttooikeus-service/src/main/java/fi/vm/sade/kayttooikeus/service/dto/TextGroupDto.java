package fi.vm.sade.kayttooikeus.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by autio on 4.10.2016.
 */
@Getter
@Setter
public class TextGroupDto implements Localizable, Serializable {
    private Long id;
    private List<TextDto> texts = new ArrayList<>();

    public TextGroupDto() {
    }

    public TextGroupDto(Long id) {
        this.id = id;
    }

    @Override
    public void put(String lang, String value) {
        Optional<TextDto> text = find(lang);
        if (text.isPresent()) {
            text.get().setText(value);
        } else {
            texts.add(new TextDto(lang, value));
        }
    }

    private Optional<TextDto> find(String lang) {
        return texts.stream().filter(t -> t.getLang().equals(lang)).findFirst();
    }

    @Override
    public String get(String lang) {
        return find(lang).map(TextDto::getText).orElse(null);
    }

    public static TextGroupDto localizeLaterById(Long id) {
        return id == null ? null : new TextGroupDto(id);
    }
}