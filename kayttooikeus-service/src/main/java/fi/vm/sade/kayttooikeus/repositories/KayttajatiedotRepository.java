package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface KayttajatiedotRepository extends CrudRepository<Kayttajatiedot, Long>, KayttajatiedotRepositoryCustom {

    Optional<Kayttajatiedot> findByHenkiloOidHenkilo(String oidHenkilo);

    long deleteByHenkilo(Henkilo henkilo);

    @Modifying
    @Transactional
    @Query(value= "DELETE FROM identification WHERE id IN (" +
            "   SELECT i.id FROM " +
            "       identification i, " +
            "       kayttajatiedot k" +
            "   WHERE " +
            "       i.idpentityid != 'haka' " +
            "       AND k.henkiloid = i.henkilo_id " +
            "       AND k.username != i.identifier)", nativeQuery = true)
    void cleanObsoletedIdentifications();
}
