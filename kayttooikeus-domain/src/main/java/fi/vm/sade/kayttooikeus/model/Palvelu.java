package fi.vm.sade.kayttooikeus.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@Table(name = "palvelu", uniqueConstraints = {@UniqueConstraint(columnNames={"name"})})
public class Palvelu extends IdentifiableAndVersionedEntity {
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "palvelutyyppi")
    private PalveluTyyppi palveluTyyppi;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "textgroup_id")
    private TextGroup description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kokoelma_id")
    private Palvelu kokoelma;

    @OneToMany(mappedBy = "kokoelma")
    private Set<Palvelu> enclosedPalvelus = new HashSet<Palvelu>();

    @OneToMany(mappedBy = "palvelu")
    private Set<KayttoOikeus> kayttoOikeus = new HashSet<KayttoOikeus>();
}
