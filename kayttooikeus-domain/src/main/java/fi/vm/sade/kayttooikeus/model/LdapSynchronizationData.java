package fi.vm.sade.kayttooikeus.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * LDAP-synkronoinnin statistiikka.
 *
 * @see LdapUpdateData jono
 */
@Entity
@Table(name = "ldap_synchronization_data")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LdapSynchronizationData extends IdentifiableAndVersionedEntity {

    private static final long serialVersionUID = -2268331380676699523L;

    @Column(name = "last_run", nullable = false)
    private LocalDateTime lastRun;

    @Column(name = "avg_update_time", nullable = false)
    private int averageUpdateTimeInMillis;

    @Column(name = "total_runtime", nullable = false)
    private int totalRuntimeInSeconds;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @Column(name = "cooloff", nullable = false)
    private boolean coolOff;

    @Column(name = "run_batch", nullable = false)
    private boolean runBatch;

    public void setAverageUpdateTime(long totalTimeInMillis, int dataSize) {
        if (totalTimeInMillis > 0 && dataSize > 0) {
            setAverageUpdateTimeInMillis((int) (totalTimeInMillis / dataSize));
        } else {
            setAverageUpdateTimeInMillis(0);
        }
    }

}
