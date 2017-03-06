package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Identification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IdentificationRepository extends CrudRepository<Identification, Long> {
    Optional<Identification> findByidpEntityIdAndIdentifier(String idpKey, String idpIdentifier);

    Optional<Identification> findByAuthtoken(String token);

    List<Identification> findByHenkiloOidHenkiloAndIdpEntityId(String oid, String idpKey);
}
