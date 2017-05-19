package fi.vm.sade.kayttooikeus.service.external;

import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloPerustietoDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkilonYhteystiedotViewDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singletonList;

public interface OppijanumerorekisteriClient {

    HenkiloDto getHenkilo(String henkiloOid);

    List<HenkiloPerustietoDto> getHenkilonPerustiedot(Collection<String> henkiloOid);

    default Optional<HenkiloPerustietoDto> getHenkilonPerustiedot(String henkiloOid) {
        if (henkiloOid == null) {
            return Optional.empty();
        }
        return getHenkilonPerustiedot(singletonList(henkiloOid)).stream()
                .filter(h -> henkiloOid.equals(h.getOidHenkilo())).findFirst();
    }
    
    HenkilonYhteystiedotViewDto getHenkilonYhteystiedot(String henkiloOid);

    Set<String> getAllOidsForSamePerson(String personOid);

    String getOidByHetu(String hetu);

    HenkiloPerustietoDto getPerustietoByOid(String oidHenkilo);

    HenkiloDto getHenkiloByOid(String oid);

    HenkilonYhteystiedotViewDto getYhteystiedotByOid(String oidHenkilo);
}
