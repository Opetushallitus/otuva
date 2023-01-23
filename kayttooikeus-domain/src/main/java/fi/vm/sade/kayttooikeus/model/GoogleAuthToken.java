package fi.vm.sade.kayttooikeus.model;

import lombok.*;

import java.time.LocalDateTime;

import javax.persistence.*;

import org.hibernate.annotations.Type;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "google_auth_token", schema = "public")
public class GoogleAuthToken {
    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue
    private Integer id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "henkilo_id", nullable = false, unique = true)
    private Henkilo henkilo;

    @Column(name = "scratch_codes", nullable = false, columnDefinition = "int[]")
    @Type(type = "fi.vm.sade.kayttooikeus.model.PostgresIntegerArrayType")
    @Builder.Default()
    private Integer[] scratchCodes = new Integer[0];

    @Column(name = "secret_key", nullable = false)
    private String secretKey;

    @Column(name = "validation_code", nullable = false)
    private Long validationCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "registration_date", nullable = false)
    @Builder.Default()
    private LocalDateTime registrationDate = LocalDateTime.now();
}
