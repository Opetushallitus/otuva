package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HaettuKayttooikeusryhmaDto {

    private Long id;

    private AnomusDto anomus;

    private KayttoOikeusRyhmaDto kayttoOikeusRyhma;

    private ZonedDateTime kasittelyPvm;

    private KayttoOikeudenTila tyyppi;
}
