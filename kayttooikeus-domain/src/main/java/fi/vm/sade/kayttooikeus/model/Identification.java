package fi.vm.sade.kayttooikeus.model;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

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

    /**
     * Voimassaoloaika.
     *
     * @deprecated Sarake löytyy henkilöpalvelun tietokannasta, mutta sille ei
     * ole tällä hetkellä käyttöä käyttöoikeuspalvelun puolella
     */
    @Column(name = "expiration_date")
    @Temporal(TemporalType.TIMESTAMP)
    @Deprecated
    private Date expirationDate;

    @Column(name = "email")
    private String email;
}
