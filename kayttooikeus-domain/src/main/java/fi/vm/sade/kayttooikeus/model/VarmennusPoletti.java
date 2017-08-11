package fi.vm.sade.kayttooikeus.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "varmennus_poletti")
public class VarmennusPoletti extends IdentifiableAndVersionedEntity {

    @Column(name = "poletti", nullable = false, unique = true)
    private String poletti;

    @Column(name = "tyyppi", nullable = false)
    @Enumerated(EnumType.STRING)
    private VarmennusPolettiTyyppi tyyppi;

    @Column(name = "voimassa", nullable = false)
    private LocalDateTime voimassa;

    @ManyToOne
    @JoinColumn(name = "henkilo_id", nullable = false)
    private Henkilo henkilo;

    public enum VarmennusPolettiTyyppi {
        HAVINNYT_SALASANA,
        SAHKOPOSTI_VARMENNUS
    }

}
