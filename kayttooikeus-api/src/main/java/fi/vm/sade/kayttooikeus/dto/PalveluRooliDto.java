package fi.vm.sade.kayttooikeus.dto;

import fi.vm.sade.kayttooikeus.dto.Localizable;
import fi.vm.sade.kayttooikeus.dto.LocalizableDto;
import fi.vm.sade.kayttooikeus.dto.TextGroupListDto;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PalveluRooliDto implements LocalizableDto {
    private String palveluName;
    private TextGroupListDto palveluTexts;
    private String rooli;
    private TextGroupListDto rooliTexts;

    public void setPalveluTextsId(Long id) {
        this.palveluTexts = TextGroupListDto.localizeAsListLaterById(id);
    }

    public void setRooliText(Long id) {
        this.rooliTexts = TextGroupListDto.localizeAsListLaterById(id);
    }
    
    @Override
    public Stream<Localizable> localizableTexts() {
        return LocalizableDto.of(palveluTexts, rooliTexts).localizableTexts();
    }
}
