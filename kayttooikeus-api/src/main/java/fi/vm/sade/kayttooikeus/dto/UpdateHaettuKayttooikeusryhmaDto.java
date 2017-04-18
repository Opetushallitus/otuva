package fi.vm.sade.kayttooikeus.dto;

import lombok.*;
import org.joda.time.LocalDate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateHaettuKayttooikeusryhmaDto {

    private Long id;

//    @Pattern(regexp = "^(HYVAKSYTTY|HYLATTY)$", message = "invalid.kayttooikeudentila")
    private KayttoOikeudenTila kayttoOikeudenTila;

    // TODO ennen loppupvm
    @NotNull
    private LocalDate alkupvm;

    @NotNull
    // TODO alkupvm jälkeen ja enintään vuosi siitä (virkailijalle) tai 10v siitä (palvelulle)
    private LocalDate loppupvm;

}
