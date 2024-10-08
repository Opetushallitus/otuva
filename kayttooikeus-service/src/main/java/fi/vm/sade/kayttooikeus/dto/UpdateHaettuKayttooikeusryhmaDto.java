package fi.vm.sade.kayttooikeus.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateHaettuKayttooikeusryhmaDto {

    // Haettu käyttöoikeusryhmä id
    private Long id;

    @Pattern(regexp = "^(MYONNETTY|HYLATTY)$", message = "invalid.kayttooikeudentila")
    private String kayttoOikeudenTila;

    @NotNull
    private LocalDate alkupvm;

    @NotNull
    private LocalDate loppupvm;

    private String hylkaysperuste;

    @JsonIgnore
    @AssertTrue(message = "invalid.date.alkupvm-after-loppupvm")
    public boolean isAlkuPvmBeforeLoppuPvm() {
        return this.alkupvm.isBefore(this.loppupvm);
    }

}
