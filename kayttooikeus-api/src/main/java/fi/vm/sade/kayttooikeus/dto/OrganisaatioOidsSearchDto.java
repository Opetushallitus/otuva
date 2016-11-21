package fi.vm.sade.kayttooikeus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class OrganisaatioOidsSearchDto {
    @NotNull
    private HenkiloTyyppi henkiloTyyppi;
    private List<String> organisaatioOids;
    private String groupName;
}
