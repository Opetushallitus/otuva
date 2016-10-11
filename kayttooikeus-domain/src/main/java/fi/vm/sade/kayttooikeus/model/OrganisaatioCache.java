package fi.vm.sade.kayttooikeus.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * User: tommiha
 * Date: 6/10/13
 * Time: 2:53 PM
 */
@Entity
@Getter @Setter
@Table(name = "organisaatio_cache")
public class OrganisaatioCache implements Serializable {
    @Id
    @Column(name = "organisaatio_oid")
    public String organisaatioOid;

    @Column(name = "organisaatio_oid_path")
    private String organisaatioOidPath;
}
