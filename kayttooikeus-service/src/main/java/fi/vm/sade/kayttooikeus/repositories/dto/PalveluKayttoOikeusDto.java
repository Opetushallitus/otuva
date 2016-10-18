package fi.vm.sade.kayttooikeus.repositories.dto;

import fi.vm.sade.kayttooikeus.service.dto.Localizable;
import fi.vm.sade.kayttooikeus.service.dto.LocalizableDto;
import fi.vm.sade.kayttooikeus.service.dto.TextGroupDto;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.stream.Stream;

import static fi.vm.sade.kayttooikeus.service.dto.TextGroupDto.localizeLaterById;

@Getter
@Setter
public class PalveluKayttoOikeusDto implements Serializable, LocalizableDto {
    private String rooli;
    private TextGroupDto oikeusLangs;

    public PalveluKayttoOikeusDto(String rooli, Long textGroupId) {
        this.rooli = rooli;
        this.oikeusLangs = localizeLaterById(textGroupId);
    }

    @Override
    public Stream<Localizable> localizableTexts() {
        return Stream.of(oikeusLangs);
    }
}
