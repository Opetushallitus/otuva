package fi.vm.sade.kayttooikeus.model;

import lombok.*;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Entity;
import javax.persistence.Table;

import static org.joda.time.DateTime.now;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "henkilo_cache_modified", schema = "public")
public class HenkiloCacheModified extends IdentifiableAndVersionedEntity {
    
    @Type(type = "dateTime")
    private DateTime modified = now();
    
}
