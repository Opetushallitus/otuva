package fi.vm.sade.kayttooikeus.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonIgnoreProperties({"kayttoOikeusRyhmaId"})
public class OrganisaatioViiteDto {
    private Long id;
    private String organisaatioTyyppi;
    @JsonIgnore
    private Long kayttoOikeusRyhmaId;
}
