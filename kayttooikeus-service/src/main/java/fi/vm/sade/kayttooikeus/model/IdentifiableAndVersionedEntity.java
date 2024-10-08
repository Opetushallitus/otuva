package fi.vm.sade.kayttooikeus.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@MappedSuperclass
public class IdentifiableAndVersionedEntity implements Identifiable {
    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
