package fi.vm.sade.kayttooikeus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KayttooikeusHenkiloCanGrantDto {
    boolean admin;

    Map<String, List<Long>> kayttooikeusByOrganisation = new HashMap<>();

    public void addToOrganisation(String organisaatioOid, List<Long> kayttooikeusryhmaId) {
        if(this.kayttooikeusByOrganisation == null) {
            this.kayttooikeusByOrganisation = new HashMap<>();
        }
        if(!kayttooikeusryhmaId.isEmpty()) {
            Optional<List<Long>> kayttooikeusryhmaIdsByOrganisation = Optional.ofNullable(this.kayttooikeusByOrganisation.get(organisaatioOid));
            if(kayttooikeusryhmaIdsByOrganisation.isPresent()) {
                kayttooikeusryhmaIdsByOrganisation.get().addAll(kayttooikeusryhmaId);
                this.kayttooikeusByOrganisation.put(organisaatioOid, kayttooikeusryhmaIdsByOrganisation.get());
            }
            else {
                this.kayttooikeusByOrganisation.put(organisaatioOid, kayttooikeusryhmaId);
            }
        }
    }
}
