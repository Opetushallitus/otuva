package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class MfaTriggerDto {
    @NotNull
    private String principalId;
    @NotNull
    private String serviceId;
}
