package fi.vm.sade.kayttooikeus.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@Table(name = "organisaatioHenkilo", uniqueConstraints = @UniqueConstraint(name = "UK_organisaatiohenkilo_01",
        columnNames = { "organisaatio_oid", "henkilo_id" }))
public class OrganisaatioHenkilo extends IdentifiableAndVersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "henkilo_id", nullable = false)
    private Henkilo henkilo;

    @Column(name = "organisaatio_oid", nullable = false)
    private String organisaatioOid;

    @Column(name = "tyyppi", nullable = true) 
    @Enumerated(EnumType.STRING)
    private OrganisaatioHenkiloTyyppi organisaatioHenkiloTyyppi;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "organisaatio_oid", insertable = false, updatable = false, nullable = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private OrganisaatioCache organisaatioCache;

    @OneToMany(mappedBy = "organisaatioHenkilo", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    private Set<MyonnettyKayttoOikeusRyhmaTapahtuma> myonnettyKayttoOikeusRyhmas = new HashSet<MyonnettyKayttoOikeusRyhmaTapahtuma>();

    @OneToMany(mappedBy = "organisaatioHenkilo", cascade = CascadeType.ALL)
    private Set<KayttoOikeusRyhmaTapahtumaHistoria> kayttoOikeusRyhmaHistorias = new HashSet<KayttoOikeusRyhmaTapahtumaHistoria>();
    
    @Column(name = "passivoitu", nullable = false)
    private boolean passivoitu;

    @Temporal(TemporalType.DATE)
    @Column(name = "voimassa_alku_pvm")
    @Type(type = "localDate")
    private LocalDate voimassaAlkuPvm;

    @Temporal(TemporalType.DATE)
    @Column(name = "voimassa_loppu_pvm")
    @Type(type = "localDate")
    private LocalDate voimassaLoppuPvm;
}
