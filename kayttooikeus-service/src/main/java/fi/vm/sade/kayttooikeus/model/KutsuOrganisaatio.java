package fi.vm.sade.kayttooikeus.model;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Kutsuun liitetyt organisaatiot ja niiden käyttöoikeudet.
 *
 * Päivitykset tehdään {@link Kutsu}-entiteetin kautta.
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "kutsu_organisaatio", schema = "public")
public class KutsuOrganisaatio extends IdentifiableAndVersionedEntity {

    @Column(name = "organisaatio_oid", nullable = false)
    private String organisaatioOid;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "kutsu", nullable = false)
    private Kutsu kutsu;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "kutsu_organisaatio_ryhma",
            joinColumns = @JoinColumn(name = "kutsu_organisaatio", nullable = false, insertable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "ryhma", nullable = false, insertable = false, updatable = false))
    @Builder.Default
    private Set<KayttoOikeusRyhma> ryhmat = new HashSet<>();

    @Column(name = "voimassa_loppu_pvm")
    private LocalDate voimassaLoppuPvm; // myönnettävien käyttöoikeuksien päättymispäivämäärä

}
