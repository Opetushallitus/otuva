package fi.vm.sade.kayttooikeus.service.dto;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class MyonnettyKayttoOikeusDTO {
    private Long ryhmaId;
    private Long myonnettyTapahtumaId;
    private String organisaatioOid;
    private String tehtavanimike;
    private List<LocaleTextDto> ryhmaNames = new ArrayList<>();
    private KayttoOikeudenTila tila;
    private String tyyppi;
    private LocalDate alkuPvm;
    private LocalDate voimassaPvm;
    private DateTime kasitelty;
    private String kasittelijaOid;
    private String kasittelijaNimi;
    private boolean selected;
    private boolean removed;
    private String muutosSyy;
}
