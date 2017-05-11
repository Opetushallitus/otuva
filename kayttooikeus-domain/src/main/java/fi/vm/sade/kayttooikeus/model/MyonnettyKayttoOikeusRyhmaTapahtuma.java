package fi.vm.sade.kayttooikeus.model;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import lombok.*;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "myonnetty_kayttooikeusryhma_tapahtuma")
public class MyonnettyKayttoOikeusRyhmaTapahtuma extends IdentifiableAndVersionedEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kayttooikeusryhma_id", nullable = false)
    private KayttoOikeusRyhma kayttoOikeusRyhma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisaatiohenkilo_id", nullable = false)
    private OrganisaatioHenkilo organisaatioHenkilo;

    @Column(name = "syy")
    private String syy;

    @Enumerated(EnumType.STRING)
    @Column(name = "tila", nullable = false)
    private KayttoOikeudenTila tila;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kasittelija_henkilo_id")
    private Henkilo kasittelija;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "aikaleima", nullable = false)
    @Type(type = "dateTime")
    private DateTime aikaleima = new DateTime();

    @Temporal(TemporalType.DATE)
    @Column(name = "voimassaalkupvm", nullable = false)
    @Type(type = "localDate")
    private LocalDate voimassaAlkuPvm;

    @Temporal(TemporalType.DATE)
    @Column(name = "voimassaloppupvm")
    @Type(type = "localDate")
    private LocalDate voimassaLoppuPvm;

    @ManyToMany(mappedBy = "myonnettyKayttooikeusRyhmas", fetch = FetchType.LAZY)
    private Set<Anomus> anomus = new HashSet<Anomus>();

    public KayttoOikeusRyhmaTapahtumaHistoria toHistoria(DateTime aikaleima, String syy) {
        return toHistoria(getKasittelija(), getTila(), aikaleima, syy);
    }

    public KayttoOikeusRyhmaTapahtumaHistoria toHistoria(Henkilo kasittelija, KayttoOikeudenTila tila, DateTime aikaleima, String syy) {
        return new KayttoOikeusRyhmaTapahtumaHistoria(
                getKayttoOikeusRyhma(),
                getOrganisaatioHenkilo(),
                syy,
                getTila(),
                kasittelija,
                aikaleima
        );
    }

}
