package fi.vm.sade.kayttooikeus.dto;

import lombok.*;
import org.joda.time.DateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HaettuKayttooikeusryhmaDto {

    private AnomusDto anomus;

    private KayttoOikeusRyhmaDto kayttoOikeusRyhma;

    private DateTime kasittelyPvm;

    private KayttoOikeudenTila tyyppi;
}
