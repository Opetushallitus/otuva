package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import org.springframework.data.repository.CrudRepository;

public interface KayttajatiedotRepository extends CrudRepository<Kayttajatiedot, Long>, KayttajatiedotRepositoryCustom {

}
