package fi.vm.sade.kayttooikeus.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@Table(name = "organisaatioviite")
public class OrganisaatioViite extends IdentifiableAndVersionedEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kayttooikeusryhma_id", nullable = false, unique = false)
    private KayttoOikeusRyhma kayttoOikeusRyhma;
    
    @Column(name = "organisaatio_tyyppi")
    private String organisaatioTyyppi;
}
