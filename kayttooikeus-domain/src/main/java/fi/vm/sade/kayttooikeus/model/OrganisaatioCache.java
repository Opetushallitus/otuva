package fi.vm.sade.kayttooikeus.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Builder
@Getter @Setter
@Table(name = "organisaatio_cache")
@Deprecated
public class OrganisaatioCache implements Serializable {
    @Id
    @Column(name = "organisaatio_oid")
    private String organisaatioOid;

    @Column(name = "organisaatio_oid_path")
    private String organisaatioOidPath;

    public OrganisaatioCache() {
    }

    public OrganisaatioCache(String organisaatioOid, String organisaatioOidPath) {
        this.organisaatioOid = organisaatioOid;
        this.organisaatioOidPath = organisaatioOidPath;
    }
}
