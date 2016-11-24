package fi.vm.sade.kayttooikeus.service.external.impl;

import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class OrganisaatioCache {
    private final OrganisaatioPerustieto root;
    private final Map<String,OrganisaatioPerustieto> byOid;

    public OrganisaatioCache(OrganisaatioPerustieto root, List<OrganisaatioPerustieto> children) {
        this.root = root;
        this.byOid = new HashMap<>();
        root.setChildren(children);
        this.byOid.put(root.getOid(), root);
        this.byOid.putAll(children.stream().flatMap(OrganisaatioPerustieto::andChildren)
            .collect(toMap(OrganisaatioPerustieto::getOid, identity())));
    }
    
    public OrganisaatioPerustieto getRoot() {
        return root;
    }
    
    public Optional<OrganisaatioPerustieto> getByOid(String oid) {
        return ofNullable(byOid.get(oid));
    }
    
    public Stream<OrganisaatioPerustieto> flatHierarchyByOid(String oid) {
        return getByOid(oid).map(OrganisaatioPerustieto::andChildren).orElse(Stream.empty());
    }
}
