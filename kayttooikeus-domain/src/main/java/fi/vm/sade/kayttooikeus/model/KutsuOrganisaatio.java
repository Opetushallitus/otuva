package fi.vm.sade.kayttooikeus.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@Table(name = "kutsu_organisaatio", schema = "public")
public class KutsuOrganisaatio extends IdentifiableAndVersionedEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kutsu", nullable = false)
    private Kutsu kutsu;
    
    @Column(name = "organisaatio_oid", nullable = false)
    private String organisaatioOid;
    
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "kutsu_organisaatio_ryhma", joinColumns = @JoinColumn(name = "kutsu_organisaatio", nullable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "ryhma", nullable = false, updatable = false))
    private Set<KayttoOikeusRyhma> ryhmat = new HashSet<>(0);
}
