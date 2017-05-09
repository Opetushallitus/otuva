package fi.vm.sade.kayttooikeus.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.joda.time.LocalDate;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

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
