package fi.vm.sade.kayttooikeus.dto;

import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Kutsujen CRUD-toiminnallisuuksille.
 *
 * @see KutsuListDto listaustoimintojen dto
 */
@Getter
@Setter
public class KutsuDto {

    private Long id;
    @NotNull
    private String sahkoposti;
    @NotNull
    private String asiointikieli;
    @Valid
    @NotNull
    private Set<KutsuOrganisaatioDto> organisaatiot;

    @Getter
    @Setter
    public static class KutsuOrganisaatioDto {

        @NotNull
        private String organisaatioOid;
        @Valid
        @NotNull
        private Set<KayttoOikeusRyhmaDto> kayttoOikeusRyhmat;

    }

    @Getter
    @Setter
    public static class KayttoOikeusRyhmaDto {

        @NotNull
        private Long id;
        private TextGroupMapDto nimi; // Huom! vain luku

    }

}
