package fi.vm.sade.kayttooikeus.service.external;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Getter
@Setter
public class OrganisaatioPerustieto {
    private String oid;
    private String parentOid;
    private String parentOidPath;
    private String ytunnus;
    private String virastotunnus;
    private String oppilaitosKoodi;
    private String oppilaitostyyppi;
    private Map<String, String> nimi = new HashMap<String, String>();
    private List<String> tyypit = new ArrayList<>();
    private List<String> kieletUris = new ArrayList<String>();
    private String kotipaikkaUri;
    private List<OrganisaatioPerustieto> children = new ArrayList<>();
    @JsonIgnore // avoid recursion if this is returned in JSON
    private OrganisaatioPerustieto parent;

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
}
