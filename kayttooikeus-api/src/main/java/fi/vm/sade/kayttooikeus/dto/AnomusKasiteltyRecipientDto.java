package fi.vm.sade.kayttooikeus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AnomusKasiteltyRecipientDto {
    private final String nimi;
    private final KayttoOikeudenTila tila;
    private final String hylkaysperuste;

    public AnomusKasiteltyRecipientDto(String nimi, KayttoOikeudenTila tila) {
        this.nimi = nimi;
        this.tila = tila;
        hylkaysperuste = null;
    }
}
