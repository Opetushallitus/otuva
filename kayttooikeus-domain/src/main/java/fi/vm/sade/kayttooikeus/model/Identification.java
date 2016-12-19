package fi.vm.sade.kayttooikeus.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "identification")
public class Identification extends IdentifiableAndVersionedEntity{

    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(name = "henkilo_id", nullable = false)
    private Henkilo henkilo;

    @Column(name = "idpentityid", nullable = false)
    private String idpEntityId;

    @Column(name = "identifier", nullable = false)
    private String identifier;

    @Column(name = "authtoken")
    private String authtoken;

    @Column(name = "expiration_date")
    private Date expirationDate;

}
