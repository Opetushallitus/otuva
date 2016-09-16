package fi.vm.sade.kayttooikeus.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Getter @Setter
@Table(name = "kayttooikeusryhma_tapahtuma_historia")
public class KayttoOikeusRyhmaTapahtumaHistoria extends IdentifiableAndVersionedEntity {

    private static final long serialVersionUID = 6182609506916140406L;

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
    private DateTime aikaleima;
}
