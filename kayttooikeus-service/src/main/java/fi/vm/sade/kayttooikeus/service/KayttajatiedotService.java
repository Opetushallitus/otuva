package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.KayttajatiedotCreateDto;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotReadDto;

public interface KayttajatiedotService {

    KayttajatiedotReadDto create(String henkiloOid, KayttajatiedotCreateDto kayttajatiedot);

    KayttajatiedotReadDto getByHenkiloOid(String henkiloOid);

    void changePasswordAsAdmin(String oid, String newPassword);
}
