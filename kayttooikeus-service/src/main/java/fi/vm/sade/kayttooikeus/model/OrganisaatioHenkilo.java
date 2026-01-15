package fi.vm.sade.kayttooikeus.model;

import lombok.*;
import org.hibernate.annotations.BatchSize;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "organisaatiohenkilo", uniqueConstraints = @UniqueConstraint(name = "UK_organisaatiohenkilo_01",
        columnNames = { "organisaatio_oid", "henkilo_id" }))
public class OrganisaatioHenkilo extends IdentifiableAndVersionedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "henkilo_id", nullable = false)
    private Henkilo henkilo;

    @Column(name = "organisaatio_oid", nullable = false)
    private String organisaatioOid;

    @OneToMany(mappedBy = "organisaatioHenkilo", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @BatchSize(size = 50)
    @Builder.Default
    private Set<MyonnettyKayttoOikeusRyhmaTapahtuma> myonnettyKayttoOikeusRyhmas = new HashSet<>();

    @OneToMany(mappedBy = "organisaatioHenkilo", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<KayttoOikeusRyhmaTapahtumaHistoria> kayttoOikeusRyhmaHistorias = new HashSet<>();

    @Column(name = "passivoitu", nullable = false)
    private boolean passivoitu;

    public void addMyonnettyKayttooikeusryhmaTapahtuma(MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma) {
        if(this.myonnettyKayttoOikeusRyhmas == null) {
            this.myonnettyKayttoOikeusRyhmas = new HashSet<>();
        }
        this.myonnettyKayttoOikeusRyhmas.add(myonnettyKayttoOikeusRyhmaTapahtuma);
    }

    public boolean isAktiivinen() {
        return !isPassivoitu();
    }
}
