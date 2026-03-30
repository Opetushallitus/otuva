package fi.vm.sade.kayttooikeus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VirkailijaRegistration {
    @NotBlank
    String hetu;

    @NotBlank
    String etunimet;

    @NotBlank
    String sukunimi;

    @NotBlank
    String token;
}