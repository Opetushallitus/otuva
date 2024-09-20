package fi.vm.sade.kayttooikeus.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Getter @Setter
@Table(name = "kayttooikeusryhma_myontoviite")
public class KayttoOikeusRyhmaMyontoViite extends IdentifiableAndVersionedEntity {

    @Column(name = "kayttooikeusryhma_master_id", nullable = false)
    private Long masterId;

    @Column(name = "kayttooikeusryhma_slave_id", nullable = false)
    private Long slaveId;
}
