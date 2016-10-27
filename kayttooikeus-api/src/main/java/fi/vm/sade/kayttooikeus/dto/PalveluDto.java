package fi.vm.sade.kayttooikeus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.stream.Stream;

import static fi.vm.sade.kayttooikeus.dto.TextGroupDto.localizeLaterById;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PalveluDto implements Serializable, LocalizableDto {
    private Long id;
    private String name;
    private PalveluTyyppi palveluTyyppi;
    private TextGroupDto description;
    private PalveluDto kokoelma;

    public PalveluDto(Long id) {
        this.id = id;
    }

    public PalveluDto() {
    }

    public void setDescriptionId(Long id) {
        this.description = localizeLaterById(id);
    }
    
    public void setKokoelmaId(Long id) {
        this.kokoelma = id == null ? null : new PalveluDto(id);
    }

    @Override
    public Stream<Localizable> localizableTexts() {
        return LocalizableDto.of(description).localizableTexts();
    }
}
