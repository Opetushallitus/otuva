package fi.vm.sade.kayttooikeus.service.external;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.vm.sade.kayttooikeus.dto.enumeration.OrganisaatioStatus;
import lombok.*;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrganisaatioPerustieto {

    @Deprecated // refaktoroi henkilo-ui käyttämään organisaatiotyyppi-koodistoa
    private static final Map<String, String> ORGANISAATIOTYYPIT;

    static {
        Map<String, String> tmp = new LinkedHashMap<>();
        tmp.put("organisaatiotyyppi_01", "KOULUTUSTOIMIJA");
        tmp.put("organisaatiotyyppi_02", "OPPILAITOS");
        tmp.put("organisaatiotyyppi_03", "TOIMIPISTE");
        tmp.put("organisaatiotyyppi_04", "OPPISOPIMUSTOIMIPISTE");
        tmp.put("organisaatiotyyppi_05", "MUU_ORGANISAATIO");
        tmp.put("organisaatiotyyppi_06", "TYOELAMAJARJESTO");
        tmp.put("organisaatiotyyppi_07", "VARHAISKASVATUKSEN_JARJESTAJA");
        tmp.put("organisaatiotyyppi_08", "VARHAISKASVATUKSEN_TOIMIPAIKKA");
        ORGANISAATIOTYYPIT = unmodifiableMap(tmp);
    }

    private String oid;
    private String parentOidPath;
    private String oppilaitostyyppi;
    private Map<String, String> nimi = new HashMap<>();
    private List<String> organisaatiotyypit = new ArrayList<>(); // GET /organisaatio/v4/hierarkia/hae palauttaa tämän
    private List<String> tyypit = new ArrayList<>(); // GET /organisaatio/v4/<oid> palauttaa tämän
    private List<OrganisaatioPerustieto> children = new ArrayList<>();
    @JsonIgnore // avoid recursion if this is returned in JSON
    private OrganisaatioPerustieto parent;
    private OrganisaatioStatus status;

    private List<String> resolveOrganisaatiotyypit() {
        if (this.organisaatiotyypit != null && !this.organisaatiotyypit.isEmpty()) {
            return this.organisaatiotyypit;
        }
        return this.tyypit;
    }

    /**
     * Palauttaa organisaatiotyypit organisaatiopalvelun v2-muodossa.
     * @return organisaatiotyypit v2-muodossa
     * @deprecated refaktoroi henkilo-ui käyttämään organisaatiotyyppi-koodistoa
     */
    @Deprecated
    public List<String> getOrganisaatiotyypit() {
        return Optional.ofNullable(organisaatiotyypit)
                .map(tyypit -> tyypit.stream().map(ORGANISAATIOTYYPIT::get).filter(Objects::nonNull).collect(toList()))
                .orElse(null);
    }

    /**
     * Palauttaa organisaatiotyypit organisaatiopalvelun v2-muodossa.
     * @return organisaatiotyypit v2-muodossa
     * @deprecated refaktoroi henkilo-ui käyttämään organisaatiotyyppi-koodistoa
     */
    @Deprecated
    public List<String> getTyypit() {
        return Optional.ofNullable(resolveOrganisaatiotyypit())
                .map(tyypit -> tyypit.stream().map(tyyppi -> {
                    if ("Ryhma".equals(tyyppi)) {
                        return "Ryhma";
                    }
                    return ORGANISAATIOTYYPIT.get(tyyppi);
                }).filter(Objects::nonNull).collect(toList()))
                .orElse(emptyList());
    }

    public boolean hasAnyOrganisaatiotyyppi(String... organisaatiotyypit) {
        List<String> organisaatiotyypitList = asList(organisaatiotyypit);
        return Optional.ofNullable(resolveOrganisaatiotyypit())
                .map(tyypit -> tyypit.stream().anyMatch(organisaatiotyypitList::contains))
                .orElse(false);
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

    /**
     * This method can be used to distinguish between organisation and group. Oid is built up
     * is such a way that both organisation types contain distinct "type-class" part.
     * Structure of oid: hard-coded-prefix.type-class.10+-digit-suffix.
     * For organisations, the type-class part is known to be "10". Given the oid structure, the substring
     * ".10." can only exist for organisation oid:s.
     *
     * @see <a href="https://wiki.eduuni.fi/display/OPHPALV/OID-Solmuluokat">documentation</a>
     * @see <a href="https://github.com/Opetushallitus/java-utils/blob/master/oid-generator/src/main/java/fi/vm/sade/oidgenerator/OIDGenerator.java">implementation</a>
     *
     * @return boolean indication whether this entity is organisation
     */
    @JsonIgnore
    public boolean isOrganisaatio() {
        return Optional.ofNullable(oid).map(str -> str.indexOf(".10.") >= 0).orElse(false);
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
