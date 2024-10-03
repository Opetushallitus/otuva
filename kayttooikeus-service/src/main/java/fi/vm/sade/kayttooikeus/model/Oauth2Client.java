package fi.vm.sade.kayttooikeus.model;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "oauth2_client", schema = "public")
public class Oauth2Client {
    @Id
    @Column(unique = true, nullable = false)
    private String id;

    @Column(nullable = false)
    private String secret;

}
