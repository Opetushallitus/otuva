package fi.vm.sade.kayttooikeus.model;

import fi.vm.sade.kayttooikeus.converter.LdapPriorityTypeConverter;
import fi.vm.sade.kayttooikeus.converter.LdapStatusTypeConverter;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * LDAP-synkronoinnin jono.
 *
 * @see LdapSynchronizationData statistiikka
 */


@Entity
@Table(name = "ldap_update_data")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LdapUpdateData extends IdentifiableAndVersionedEntity {

    private static final long serialVersionUID = -3805645382407127555L;

    @Column(name = "priority", nullable = false)
    @Convert(converter = LdapPriorityTypeConverter.class)
    private LdapPriorityType priority;

    @Column(name = "henkilo_oid")
    private String henkiloOid;

    @Column(name = "kor_id")
    private Long kayttoOikeusRyhmaId;

    @Column(name = "status", nullable = false)
    @Convert(converter = LdapStatusTypeConverter.class)
    private LdapStatusType status;

    @Column(name = "modified", nullable = false)
    private LocalDateTime modified;

}
