package fi.vm.sade.kayttooikeus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.stream.Stream;

import static fi.vm.sade.kayttooikeus.dto.TextGroupListDto.localizeAsListLaterById;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PalveluKayttoOikeusDto implements Serializable, LocalizableDto {
    private String rooli;
    private TextGroupListDto oikeusLangs;

    public PalveluKayttoOikeusDto(String rooli, Long textGroupId) {
        this.rooli = rooli;
        this.oikeusLangs = localizeAsListLaterById(textGroupId);
    }

    @Override
    public Stream<Localizable> localizableTexts() {
        return LocalizableDto.of(oikeusLangs).localizableTexts();
    }
}
