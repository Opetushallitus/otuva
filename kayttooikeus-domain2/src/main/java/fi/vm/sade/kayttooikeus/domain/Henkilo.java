package fi.vm.sade.kayttooikeus.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 13.10
 */
@Entity
@Getter @Setter
@Table(name = "henkilo", schema = "public")
public class Henkilo {
    
    @Column(nullable = false)
    private String oidHenkilo;

    @Column(name = "passivoitu", nullable = false)
    private boolean passivoitu;

    @Column(name = "henkiloTyyppi", nullable = false)
    @Enumerated(EnumType.STRING)
    private HenkiloTyyppi henkiloTyyppi;

    @OneToOne(mappedBy = "henkilo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Kayttajatiedot kayttajatiedot;
}
