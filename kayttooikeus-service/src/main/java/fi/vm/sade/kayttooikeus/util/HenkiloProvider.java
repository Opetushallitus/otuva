package fi.vm.sade.kayttooikeus.util;

import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import java.util.HashMap;
import java.util.Map;

public class HenkiloProvider {

    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;
    private final Map<String, HenkiloDto> henkilotByOid = new HashMap<>();

    public HenkiloProvider(OppijanumerorekisteriClient oppijanumerorekisteriClient) {
        this.oppijanumerorekisteriClient = oppijanumerorekisteriClient;
    }

    public HenkiloDto getByOid(String oid) {
        return henkilotByOid.computeIfAbsent(oid, oppijanumerorekisteriClient::getHenkiloByOid);
    }

}
