package fi.vm.sade.kayttooikeus.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

/**
 * Class that contains {@link Henkilo}'s password hash and salt. Only on may
 * exist per {@link Henkilo}
 * 
 * @author kkammone
 * 
 */
@Entity
@Getter @Setter
@Table(name = "kayttajatiedot") 
public class Kayttajatiedot extends IdentifiableAndVersionedEntity {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "henkiloid", nullable = false, unique = true)
    private Henkilo henkilo;
    
    /**
     * Username for Henkilo
     */
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    /**
     * this is the permanent security token "salted hash", authtokens under
     * identification are used for temporary authentication
     * 
     * Null token disables weak login
     */
    @Column(name = "password")
    private String password;

    /**
     * Salt used for securityToken
     */
    @Column(name = "salt")
    private String salt;

    /**
     * Can be used to invalidate password for being too old
     */
    @Column(name = "createdAt")
    @Type(type = "dateTime")
    private DateTime createdAt;

    /**
     * Manually invalidated password
     */
    @Column(name = "invalidated")
    private Boolean invalidated = false;

    @PrePersist
    @PreUpdate
    public void setPersistDate() {
        createdAt = new DateTime();
    }

    @Override
    public String toString() {
        return "Kayttajatiedot{" +
                "username='" + username + '\'' +
                ", invalidated=" + invalidated +
                ", createdAt=" + createdAt +
                '}';
    }
}
