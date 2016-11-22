package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HenkiloHibernateRepository extends BaseRepository<Henkilo> {
    List<String> findHenkiloOids(HenkiloTyyppi henkiloTyyppi, List<String> ooids, String groupName);
}
