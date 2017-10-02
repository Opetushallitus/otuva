package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KayttooikeusPerustiedotDto {
    String oidHenkilo;
    Set<KayttooikeusOrganisaatiotDto> kayttooikeusOrganisaatiotDtoSet = new HashSet<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KayttooikeusOrganisaatiotDto {
        String organisaatioOid;
        Set<KayttooikeusOikeudetDto> kayttooikeusOikeudetDtoSet = new HashSet<>();

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class KayttooikeusOikeudetDto {
            String palvelu;
            String oikeus;
        }
    }
}
