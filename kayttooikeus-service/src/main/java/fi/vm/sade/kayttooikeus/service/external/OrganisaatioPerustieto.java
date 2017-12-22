package fi.vm.sade.kayttooikeus.service.external;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioStatus;
import lombok.*;

import java.util.*;
import java.util.stream.Stream;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrganisaatioPerustieto {
    private String oid;
    private String parentOidPath;
    private String oppilaitostyyppi;
    private Map<String, String> nimi = new HashMap<>();
    private List<String> organisaatiotyypit = new ArrayList<>();
    private List<String> tyypit = new ArrayList<>();
    private List<OrganisaatioPerustieto> children = new ArrayList<>();
    @JsonIgnore // avoid recursion if this is returned in JSON
    private OrganisaatioPerustieto parent;
    private OrganisaatioStatus status;
    
    public List<String> getTyypit() {
        if (this.organisaatiotyypit != null && !this.organisaatiotyypit.isEmpty()) {
            return this.organisaatiotyypit;
        }
        return this.tyypit;
    }

    /**
     * @return oppilaitostyypin koodi
     */
    public Optional<String> getOppilaitostyyppiKoodi() {
        return Optional.ofNullable(oppilaitostyyppi)
                // oppilaitostyyppi on muotoa "oppilaitostyyppi_01#1"
                // eli "<koodisto>_<koodi>#<versio>"
                .filter(tyyppi -> tyyppi.length() >= 19)
                .map(tyyppi -> tyyppi.substring(17, 19));
    }

    public Stream<OrganisaatioPerustieto> andChildren() {
        return Stream.concat(Stream.of(this),
                children.stream().flatMap(OrganisaatioPerustieto::andChildren));
    }

    public Stream<OrganisaatioPerustieto> andParents() {
        return Stream.concat(Stream.of(this), parents());
    }

    public Stream<OrganisaatioPerustieto> parents() {
        return parent == null ? Stream.empty() : parent.andParents();
    }
    
    public int getHierarchyLevel() {
        int level = 1;
        OrganisaatioPerustieto node = this;
        while (node.parent != null) {
            ++level;
            node = node.parent;
        }
        return level;
    }
}
