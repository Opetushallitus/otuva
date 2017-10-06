package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KayttooikeusPerustiedotDto {
    String oidHenkilo;
    Set<KayttooikeusOrganisaatiotDto> kayttooikeusOrganisaatiotDtoSet = new HashSet<>();

    public KayttooikeusPerustiedotDto(String oidHenkilo, String organisaatioOid, String palvelu, String oikeus) {
        this.oidHenkilo = oidHenkilo;
        KayttooikeusOrganisaatiotDto.KayttooikeusOikeudetDto kayttooikeusOikeudetDto
                = new KayttooikeusOrganisaatiotDto.KayttooikeusOikeudetDto(palvelu, oikeus);
        KayttooikeusOrganisaatiotDto kayttooikeusOrganisaatiotDto = new KayttooikeusOrganisaatiotDto(
                organisaatioOid,
                new HashSet<KayttooikeusOrganisaatiotDto.KayttooikeusOikeudetDto>() {{add(kayttooikeusOikeudetDto);}}
        );
        this.kayttooikeusOrganisaatiotDtoSet.add(kayttooikeusOrganisaatiotDto);
    }

    public KayttooikeusPerustiedotDto mergeIfSameOid(KayttooikeusPerustiedotDto kayttooikeusPerustiedotDto) {
        if(kayttooikeusPerustiedotDto.getOidHenkilo().equals(this.getOidHenkilo())) {
            this.kayttooikeusOrganisaatiotDtoSet.addAll(kayttooikeusPerustiedotDto.getKayttooikeusOrganisaatiotDtoSet());
            this.setKayttooikeusOrganisaatiotDtoSet(this.getKayttooikeusOrganisaatiotDtoSet()
                    .stream()
                    .collect(Collectors.groupingBy(KayttooikeusOrganisaatiotDto::getOrganisaatioOid))
                    .values()
                    .stream()
                    .map(kayttooikeusOrganisaatiotDtoGroup -> kayttooikeusOrganisaatiotDtoGroup
                            .stream()
                            .reduce(KayttooikeusOrganisaatiotDto::mergeIfSameOid).get())
                    .collect(Collectors.toSet()));
        }
        return this;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KayttooikeusOrganisaatiotDto {
        String organisaatioOid;
        Set<KayttooikeusOikeudetDto> kayttooikeusOikeudetDtoSet = new HashSet<>();

        public KayttooikeusOrganisaatiotDto mergeIfSameOid(KayttooikeusOrganisaatiotDto kayttooikeusOrganisaatiotDto) {
            if(kayttooikeusOrganisaatiotDto.getOrganisaatioOid().equals(this.getOrganisaatioOid())) {
                this.kayttooikeusOikeudetDtoSet.addAll(kayttooikeusOrganisaatiotDto.getKayttooikeusOikeudetDtoSet());
                this.setKayttooikeusOikeudetDtoSet(this.getKayttooikeusOikeudetDtoSet()
                        .stream()
                        .distinct()
                        .collect(Collectors.toSet()));
            }
            return this;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @EqualsAndHashCode
        public static class KayttooikeusOikeudetDto {
            String palvelu;
            String oikeus;
        }
    }
}
