package fi.vm.sade.kayttooikeus.model;


import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.ZonedDateTime;


@Entity
@Table(name = "ldap_update_data")
@Getter
@Setter
public class LdapUpdateData extends IdentifiableAndVersionedEntity{

    private static final long serialVersionUID = -3805645382407127555L;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "henkilo_oid")
    private String henkiloOid;

    @Column(name = "kor_id")
    private Long korId;

    @Column(name = "status", nullable = false)
    private int status;
    
    @Column(name = "modified", nullable = false)
    private ZonedDateTime modified;

}
