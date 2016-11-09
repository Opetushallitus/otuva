package fi.vm.sade.kayttooikeus.model;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Getter @Setter
@Table(name = "haettu_kayttooikeusryhma")
public class HaettuKayttoOikeusRyhma extends IdentifiableAndVersionedEntity {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anomus_id")
    private Anomus anomus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kayttooikeusryhma_id")
    private KayttoOikeusRyhma kayttoOikeusRyhma;

    @Column(name = "kasittelypvm")
    @Type(type = "dateTime")
    private DateTime kasittelyPvm;

    @Enumerated(EnumType.STRING)
    @Column(name = "tyyppi")
    private KayttoOikeudenTila tyyppi;
}