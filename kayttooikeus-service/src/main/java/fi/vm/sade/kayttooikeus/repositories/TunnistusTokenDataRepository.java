package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.TunnistusToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.MANDATORY)
@Repository
public interface TunnistusTokenDataRepository extends CrudRepository<TunnistusToken, Long> {

}
