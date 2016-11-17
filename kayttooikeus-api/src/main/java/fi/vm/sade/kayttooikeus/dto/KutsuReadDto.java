package fi.vm.sade.kayttooikeus.dto;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KutsuReadDto {

    private Long id;
    private String sahkoposti;
    private String asiointikieli;
    private Set<KutsuOrganisaatioDto> organisaatiot;

    @Getter
    @Setter
    public static class KutsuOrganisaatioDto {

        private String organisaatioOid;
        private Set<KayttoOikeusRyhmaDto> kayttoOikeusRyhmat;

    }

    @Getter
    @Setter
    public static class KayttoOikeusRyhmaDto {

        private Long id;
        private TextGroupMapDto nimi;

    }

}
