package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.KayttajatiedotCreateDto;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotReadDto;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotUpdateDto;
import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService.LdapSynchronizationType;

public interface KayttajatiedotService {

    KayttajatiedotReadDto create(String henkiloOid, KayttajatiedotCreateDto kayttajatiedot, LdapSynchronizationType ldapSynchronization);

    KayttajatiedotReadDto getByHenkiloOid(String henkiloOid);

    KayttajatiedotReadDto updateKayttajatiedot(String henkiloOid, KayttajatiedotUpdateDto kayttajatiedot);

    void changePasswordAsAdmin(String oid, String newPassword);

    void throwIfUsernameExists(String username);

    void throwIfUsernameIsNotValid(String username);
}
