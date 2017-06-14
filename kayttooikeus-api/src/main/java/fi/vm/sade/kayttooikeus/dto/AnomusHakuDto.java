package fi.vm.sade.kayttooikeus.dto;

import fi.vm.sade.kayttooikeus.dto.types.AnomusTyyppi;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AnomusHakuDto {

    private Long id;
    private HenkiloDto anoja;
    private HenkiloDto kasittelija;
    private OrganisaatioDto organisaatio;
    private AnomusTyyppi anomusTyyppi;
    private String anomuksenTila;
    private LocalDateTime anottuPvm;
    private LocalDateTime anomusTilaTapahtumaPvm;
    private List<HaettuKayttoOikeusRyhmaDto> haetutKayttoOikeusRyhmat;

    @Getter
    @Setter
    @Builder
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HenkiloDto {

        private String oid;
        private String etunimet;
        private String sukunimi;
        private String kayttajatunnus;

    }

    @Getter
    @Setter
    @Builder
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrganisaatioDto {

        private String oid;
        private Map<String, String> nimi;

    }

    @Getter
    @Setter
    @Builder
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HaettuKayttoOikeusRyhmaDto {

        private Long id;
        private LocalDateTime kasittelyPvm;
        private KayttoOikeudenTila tyyppi;
        private KayttoOikeusRyhmaDto kayttoOikeusRyhma;

    }

    @Getter
    @Setter
    @Builder
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KayttoOikeusRyhmaDto {

        private Long id;
        private TextGroupMapDto nimi;

    }

}
