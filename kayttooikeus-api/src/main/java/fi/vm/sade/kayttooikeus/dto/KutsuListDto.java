package fi.vm.sade.kayttooikeus.dto;

import lombok.*;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class KutsuListDto implements Serializable {
    private Long id;
    private KutsunTila tila;
    private String etunimi;
    private String sukunimi;
    private String sahkoposti;
    private DateTime aikaleima;
    private List<KutsuOrganisaatioListDto> organisaatiot = new ArrayList<>();
}
