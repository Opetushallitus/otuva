package fi.vm.sade.kayttooikeus.dto;

import lombok.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.stream.Stream;

import static fi.vm.sade.kayttooikeus.dto.TextGroupListDto.localizeAsListLaterById;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KayttoOikeusHistoriaDto implements Serializable, LocalizableDto {
    private KayttoOikeusTyyppi tyyppi = KayttoOikeusTyyppi.KOOSTEROOLI;
    private KayttoOikeudenTila tila;
    private LocalDate voimassaAlkuPvm;
    private LocalDate voimassaLoppuPvm;
    private DateTime aikaleima;
    private String kasittelija;
    private String organisaatioOid;
    private String tehtavanimike;
    private long kayttoOikeusRyhmaId;
    private TextGroupListDto kuvaus;
    private String rooli;
    private long kayttoOikeusId;
    private TextGroupListDto kayttoOikeusKuvaus;
    private String palvelu;
    private TextGroupListDto palveluKuvaus;
    
    public void setKuvausId(Long id) {
        this.kuvaus = localizeAsListLaterById(id);
    }

    public void setKayttoOikeusKuvausId(Long id) {
        this.kayttoOikeusKuvaus = localizeAsListLaterById(id);
    }

    public void setPalveluKuvausId(Long id) {
        this.palveluKuvaus = localizeAsListLaterById(id);
    }
    
    @Override
    public Stream<Localizable> localizableTexts() {
        return LocalizableDto.of(kuvaus, kayttoOikeusKuvaus, palveluKuvaus).localizableTexts();
    }
}
