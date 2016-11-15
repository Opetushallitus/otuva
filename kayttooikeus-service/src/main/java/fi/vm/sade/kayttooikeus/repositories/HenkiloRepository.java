package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.HenkiloTyyppi;

import java.util.List;

public interface HenkiloRepository extends BaseRepository<Henkilo> {
    List<String> findHenkiloOids(HenkiloTyyppi henkiloTyyppi, List<String> ooids, String groupName);
}
