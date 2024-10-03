package fi.vm.sade.kayttooikeus.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.sade.kayttooikeus.model.Oauth2Client;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface Oauth2ClientRepository extends CrudRepository<Oauth2Client, String> {
}
