package fi.vm.sade.kayttooikeus.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 
 * @author kkammone
 * 
 */
@Entity
@Getter @Setter
@Table(name = "haettu_kayttooikeus")
public class HaettuKayttoOikeus extends IdentifiableAndVersionedEntity {
    private static final long serialVersionUID = 1L;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anomus_id")
    private Anomus anomus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kayttooikeus_id")
    private KayttoOikeus kayttoOikeus;
}