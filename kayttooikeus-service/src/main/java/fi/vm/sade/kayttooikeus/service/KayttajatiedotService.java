package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.KayttajatiedotCreateDto;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotReadDto;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotUpdateDto;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService.LdapSynchronizationType;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface KayttajatiedotService {

    KayttajatiedotReadDto create(String henkiloOid, KayttajatiedotCreateDto kayttajatiedot, LdapSynchronizationType ldapSynchronization);

    @Transactional
    void createOrUpdateUsername(String oidHenkilo, String username, LdapSynchronizationType ldapSynchronization);

    @Transactional(readOnly = true)
    Optional<Kayttajatiedot> getKayttajatiedotByOidHenkilo(String oidHenkilo);

    KayttajatiedotReadDto getByHenkiloOid(String henkiloOid);

    KayttajatiedotReadDto updateKayttajatiedot(String henkiloOid, KayttajatiedotUpdateDto kayttajatiedot);

    void changePasswordAsAdmin(String oid, String newPassword);

    void throwIfUsernameExists(String username);

    void throwIfUsernameIsNotValid(String username);
}
