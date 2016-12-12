package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.KayttajatiedotReadDto;
import java.util.Optional;

public interface KayttajatiedotRepositoryCustom {

    Optional<KayttajatiedotReadDto> findByHenkiloOid(String henkiloOid);

}
