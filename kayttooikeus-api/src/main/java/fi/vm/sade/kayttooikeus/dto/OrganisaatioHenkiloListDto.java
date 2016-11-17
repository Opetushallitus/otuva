package fi.vm.sade.kayttooikeus.dto;

import lombok.*;
import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganisaatioHenkiloListDto implements Serializable {
    private long id;
    private OrganisaatioDto organisaatio;
    private OrganisaatioHenkiloTyyppi tyyppi;
    private boolean passivoitu;
    private LocalDate voimassaAlkuPvm;
    private LocalDate voimassaLoppuPvm;
    private String tehtavanimike;
    
    public void setOrganisaatioOid(String oid) {
        if (organisaatio == null) {
            organisaatio = new OrganisaatioDto();
        }
        organisaatio.setOid(oid);
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganisaatioDto {
        private String oid;
        private TextGroupMapDto nimi;
        private List<String> tyypit;
    }
}
