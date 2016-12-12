package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.KayttajatiedotReadDto;

public interface KayttajatiedotService {

    KayttajatiedotReadDto getByHenkiloOid(String henkiloOid);

}
