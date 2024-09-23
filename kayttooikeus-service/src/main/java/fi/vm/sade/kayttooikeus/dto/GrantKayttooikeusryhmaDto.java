package fi.vm.sade.kayttooikeus.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GrantKayttooikeusryhmaDto {

    private Long id;

    @NotNull
    private LocalDate alkupvm;

    @NotNull
    private LocalDate loppupvm;

    @JsonIgnore
    @AssertTrue(message = "invalid.date.alkupvm-after-loppupvm")
    public boolean isAlkuPvmBeforeLoppuPvm() {
        return this.alkupvm.isBefore(this.loppupvm);
    }

}
