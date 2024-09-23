package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KutsuCreateDto {

    private String kutsujaOid;

    private String kutsujaForEmail;

    @NotEmpty
    private String etunimi;
    @NotEmpty
    private String sukunimi;
    @NotNull
    @Email
    private String sahkoposti;
    @NotNull
    private Asiointikieli asiointikieli;

    private String saate;

    @Valid
    @NotNull
    private Set<KutsuOrganisaatioCreateDto> organisaatiot;

    @Getter
    @Setter
    public static class KutsuOrganisaatioCreateDto {
        @NotNull
        private String organisaatioOid;
        @Valid
        @NotNull
        private Set<KutsuKayttoOikeusRyhmaCreateDto> kayttoOikeusRyhmat;
        @FutureOrPresent
        private LocalDate voimassaLoppuPvm;
    }

    @Getter
    @Setter
    public static class KutsuKayttoOikeusRyhmaCreateDto {
        @NotNull
        private Long id;
    }
}
