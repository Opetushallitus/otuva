package fi.vm.sade.kayttooikeus.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 13.57
 */
@Entity
@Getter @Setter
@Table(name = "anomus", schema = "public")
public class Anomus extends IdentifiableAndVersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "henkilo_id")
    private Henkilo henkilo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kasittelija_henkilo_id")
    private Henkilo kasittelija;

    @Column(name = "organisaatioOid")
    private String organisaatioOid;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "organisaatioOid", insertable = false, updatable = false)
    private OrganisaatioCache organisaatioCache;

    @Column(name = "tehtavanimike")
    private String tehtavanimike;

    @Column(name = "anomusTyyppi")
    @Enumerated(EnumType.STRING)
    private AnomusTyyppi anomusTyyppi;

    @Column(name = "anomuksenTila")
    @Enumerated(EnumType.STRING)
    private AnomuksenTila anomuksenTila;

    @Column(name = "anottuPvm")
    @Type(type="dateTime")
    private DateTime anottuPvm;

    @Column(name = "anomusTilaTapahtumaPvm")
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
    private Set<HaettuKayttoOikeusRyhma> haettuKayttoOikeusRyhmas = new HashSet<HaettuKayttoOikeusRyhma>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "anomus_myonnettyKayttooikeusRyhmas", joinColumns = @JoinColumn(name = "anomus_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "myonnettyKayttooikeusRyhma_id", referencedColumnName = "id"))
    private Set<MyonnettyKayttoOikeusRyhmaTapahtuma> myonnettyKayttooikeusRyhmas = new HashSet<MyonnettyKayttoOikeusRyhmaTapahtuma>();
}
