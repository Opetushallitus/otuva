package fi.vm.sade.kayttooikeus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PalvelukayttajaReadDto {
    private String oid;
    private String nimi;
    private String kayttajatunnus;
}
