package fi.vm.sade.kayttooikeus.dto;

import fi.vm.sade.kayttooikeus.dto.types.AnomusTyyppi;
import lombok.*;
import org.joda.time.DateTime;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnomusDto {

    private String organisaatioOid;

    private DateTime anottuPvm;

    private Date anomusTilaTapahtumaPvm;

    private AnomusTyyppi anomusTyyppi;

}
