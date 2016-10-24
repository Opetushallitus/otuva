package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.stream.Stream;

import static fi.vm.sade.kayttooikeus.dto.TextGroupListDto.localizeAsListLaterById;

@Getter
@Setter
public class KayttoOikeusHistoriaDto implements Serializable, LocalizableDto {
    private String organisaatioOid;
    private long kayttoOikeusId;
    private String tehtavanimike;
    private TextGroupListDto kuvaus;
    private KayttoOikeusTyyppi tyyppi = KayttoOikeusTyyppi.KOOSTEROOLI;
    private KayttoOikeudenTila tila;
    private LocalDate voimassaAlkuPvm;
    private LocalDate voimassaLoppuPvm;
    private DateTime aikaleima;
    private String kasittelija;
    
    public void setKuvausId(Long id) {
        this.kuvaus = localizeAsListLaterById(id);
    }

    @Override
    public Stream<Localizable> localizableTexts() {
        return LocalizableDto.of(kuvaus).localizableTexts();
    }
}
