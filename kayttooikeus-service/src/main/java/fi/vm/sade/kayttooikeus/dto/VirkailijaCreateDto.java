package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class VirkailijaCreateDto {

    @NotBlank
    private String etunimet;

    @NotBlank
    private String kutsumanimi;

    @NotBlank
    private String sukunimi;

    @NotBlank
    private String kayttajatunnus;

    @NotBlank
    private String salasana;

    private boolean vahvastiTunnistettu;

}
