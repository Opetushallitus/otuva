package fi.vm.sade.kayttooikeus.model;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Table;

import java.time.ZonedDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "henkilo_cache_modified", schema = "public")
public class HenkiloCacheModified extends IdentifiableAndVersionedEntity {
    
    private ZonedDateTime modified = ZonedDateTime.now();
    
}
