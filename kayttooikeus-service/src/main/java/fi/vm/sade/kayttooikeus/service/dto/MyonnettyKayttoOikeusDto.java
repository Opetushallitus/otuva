package fi.vm.sade.kayttooikeus.service.dto;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.dto.Localizable;
import fi.vm.sade.kayttooikeus.dto.LocalizableDto;
import fi.vm.sade.kayttooikeus.dto.TextGroupDto;
import lombok.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.stream.Stream;

import static fi.vm.sade.kayttooikeus.dto.TextGroupDto.localizeLaterById;

@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyonnettyKayttoOikeusDto implements LocalizableDto, Serializable{
    private Long ryhmaId;
    private Long myonnettyTapahtumaId;
    private String organisaatioOid;
    private String tehtavanimike;
    private TextGroupDto ryhmaNames;
    private KayttoOikeudenTila tila;
    private String tyyppi = "KORyhma";
    private LocalDate alkuPvm;
    private LocalDate voimassaPvm;
    private DateTime kasitelty;
    private String kasittelijaOid;
    private String kasittelijaNimi;
    private boolean selected;
    private boolean removed;
    private String muutosSyy;

    public void setRyhmaNamesId(Long ryhmaNamesId) {
        this.ryhmaNames = localizeLaterById(ryhmaNamesId);
    }

    @Override
    public Stream<Localizable> localizableTexts() {
        return LocalizableDto.of(ryhmaNames).localizableTexts();
    }

}
