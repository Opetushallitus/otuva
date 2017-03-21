package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface KayttajatiedotRepository extends CrudRepository<Kayttajatiedot, Long>, KayttajatiedotRepositoryCustom {

    Optional<Kayttajatiedot> findByUsername(String username);

    Optional<Kayttajatiedot> findByHenkiloOidHenkilo(String oidHenkilo);

}
