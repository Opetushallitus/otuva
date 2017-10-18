package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static fi.vm.sade.kayttooikeus.dto.TextGroupDto.localizeLaterById;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KayttoOikeusRyhmaDto implements Serializable, LocalizableDto {
    private Long id;
    private String name; // TODO: "tunniste" (Huom! API-muutos)
    private String rooliRajoite;
    private List<OrganisaatioViiteDto> organisaatioViite = new ArrayList<>();
    private TextGroupDto description; // TODO: "nimi" (Huom! API-muutos)
    private TextGroupDto kuvaus;

    public void setDescriptionId(Long id) {
        this.description = localizeLaterById(id);
    }

    public void setKuvausId(Long id) {
        this.kuvaus = localizeLaterById(id);
    }

    @Override
    public Stream<Localizable> localizableTexts() {
        return LocalizableDto.of(description, kuvaus).localizableTexts();
    }
}
