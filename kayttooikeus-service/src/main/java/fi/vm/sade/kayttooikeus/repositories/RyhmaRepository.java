package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Ryhma;
import java.util.List;
import java.util.Optional;
import org.springframework.data.ldap.repository.LdapRepository;

public interface RyhmaRepository extends LdapRepository<Ryhma>, RyhmaRepositoryCustom {

    Optional<Ryhma> findByNimi(String nimi);

    List<Ryhma> findByJasenet(String jasenet);

}
