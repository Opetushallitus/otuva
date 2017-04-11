package fi.vm.sade.kayttooikeus.model;

import fi.vm.sade.kayttooikeus.dto.types.AnomusTyyppi;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@Builder
@Table(name = "anomus", schema = "public")
public class Anomus extends IdentifiableAndVersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "henkilo_id")
    private Henkilo henkilo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kasittelija_henkilo_id")
    private Henkilo kasittelija;

    @Column(name = "organisaatiooid")
    private String organisaatioOid;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "organisaatiooid", insertable = false, updatable = false)
    private OrganisaatioCache organisaatioCache;

    @Column(name = "tehtavanimike")
    private String tehtavanimike;

    @Column(name = "anomustyyppi")
    @Enumerated(EnumType.STRING)
    private AnomusTyyppi anomusTyyppi;

    @Column(name = "anomuksentila")
    @Enumerated(EnumType.STRING)
    private AnomuksenTila anomuksenTila;

    @Column(name = "anottupvm")
    @Type(type="dateTime")
    private DateTime anottuPvm;

    @Column(name = "anomustilatapahtumapvm")
    private Date anomusTilaTapahtumaPvm;

    @Column(name = "perustelut")
    private String perustelut;

    @Column(name = "sahkopostiosoite", nullable = false)
    private String sahkopostiosoite;

    @Column(name = "puhelinnumero")
    private String puhelinnumero;

    @Column(name = "matkapuhelinnumero")
    private String matkapuhelinnumero;

    @Column(name = "hylkaamisperuste")
    private String hylkaamisperuste;

    @OneToMany(mappedBy = "anomus", fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    private Set<HaettuKayttoOikeusRyhma> haettuKayttoOikeusRyhmas = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "anomus_myonnettykayttooikeusryhmas", joinColumns = @JoinColumn(name = "anomus_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "myonnettykayttooikeusryhma_id", referencedColumnName = "id"))
    private Set<MyonnettyKayttoOikeusRyhmaTapahtuma> myonnettyKayttooikeusRyhmas = new HashSet<>();
}
