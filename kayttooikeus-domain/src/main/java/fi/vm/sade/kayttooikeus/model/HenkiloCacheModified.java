package fi.vm.sade.kayttooikeus.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "henkilo_cache_modified", schema = "public")
public class HenkiloCacheModified extends IdentifiableAndVersionedEntity {
    
    private LocalDateTime modified = LocalDateTime.now();
    
}
