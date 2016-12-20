package fi.vm.sade.kayttooikeus.dto;

import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
public class KutsuCreateDto {
    @NotEmpty
    private String etunimi;
    @NotEmpty
    private String sukunimi;
    @NotNull
    @Email
    private String sahkoposti;
    @NotNull
    private Asiointikieli asiointikieli;
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
    }
}
