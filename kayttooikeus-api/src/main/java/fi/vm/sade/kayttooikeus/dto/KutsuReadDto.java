package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KutsuReadDto {
    private Long id;
    private KutsunTila tila;
    private String etunimi;
    private String sukunimi;
    private String sahkoposti;
    private LocalDateTime aikaleima;
    private Asiointikieli asiointikieli;
    @Builder.Default
    private Set<KutsuOrganisaatioDto> organisaatiot = new HashSet<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KutsuOrganisaatioDto extends LocalizableOrganisaatio {
        private Set<KayttoOikeusRyhmaDto> kayttoOikeusRyhmat;

        public KutsuOrganisaatioDto(TextGroupMapDto nimi, String organisaatioOid, Set<KayttoOikeusRyhmaDto> kayttoOikeusRyhmat) {
            this.nimi = nimi;
            this.organisaatioOid = organisaatioOid;
            this.kayttoOikeusRyhmat = kayttoOikeusRyhmat;
        }
    }

    @Getter
    @Setter
    public static class KayttoOikeusRyhmaDto {
        private Long id;
        private TextGroupMapDto nimi;

    }

}
