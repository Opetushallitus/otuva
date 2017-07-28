package fi.vm.sade.kayttooikeus.model;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Column(name = "aikaleima", nullable = false)
    private LocalDateTime aikaleima = LocalDateTime.now();

    @Column(name = "voimassaalkupvm", nullable = false)
    private LocalDate voimassaAlkuPvm;

    @Column(name = "voimassaloppupvm")
    private LocalDate voimassaLoppuPvm;

    @ManyToMany(mappedBy = "myonnettyKayttooikeusRyhmas", fetch = FetchType.LAZY)
    private Set<Anomus> anomus = new HashSet<>();

    public KayttoOikeusRyhmaTapahtumaHistoria toHistoria(LocalDateTime aikaleima, String syy) {
        return toHistoria(getKasittelija(), getTila(), aikaleima, syy);
    }

    public KayttoOikeusRyhmaTapahtumaHistoria toHistoria(Henkilo kasittelija, KayttoOikeudenTila tila, LocalDateTime aikaleima, String syy) {
        return new KayttoOikeusRyhmaTapahtumaHistoria(
                getKayttoOikeusRyhma(),
                getOrganisaatioHenkilo(),
                syy,
                tila,
                kasittelija,
                aikaleima
        );
    }

    public void addAnomus(Anomus anomus) {
        if (this.anomus == null) {
            this.anomus = new HashSet<>();
        }
        this.anomus.add(anomus);
    }
}
