package fi.vm.sade.kayttooikeus.model;

import fi.vm.sade.kayttooikeus.dto.KutsunTila;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.joda.time.DateTime.now;

@Entity
@Getter @Setter
@Table(name = "kutsu", schema = "public")
public class Kutsu extends IdentifiableAndVersionedEntity {

    @Type(type = "dateTime")
    @Column(name = "aikaleima", nullable = false)
    private DateTime aikaleima = now();
    
    @Column(name = "kutsuja_oid", nullable = false)
    private String kutsuja;
    
    @Column(name = "kieli_koodi", nullable = false)
    private String kieliKoodi;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tila", nullable = false)
    private KutsunTila tila = KutsunTila.AVOIN;
    
    @Column(name = "sahkoposti", nullable = false) 
    private String sahkoposti;
    
    @OneToMany(mappedBy = "kutsu",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<KutsuOrganisaatio> organisaatiot = new HashSet<>(0);
    
    @Column(name = "salaisuus") 
    private String salsisuus; // verification hash
    
    @Type(type = "dateTime")
    private DateTime kaytetty;
    
    @Type(type = "dateTime")
    private DateTime poistettu;
    
    @Column(name = "poistaja_oid")
    private String poistaja;

    @Column(name = "luotu_henkilo_oid", nullable = true)
    private String luotuHenkiloOid;
}
