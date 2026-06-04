package fi.vm.sade.kayttooikeus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VirkailijaRegistration {
    @NotBlank
    String hetu;

    String etunimet;

    String sukunimi;

    @NotBlank
    String token;
}