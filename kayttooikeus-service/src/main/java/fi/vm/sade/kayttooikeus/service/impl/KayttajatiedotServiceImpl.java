package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotCreateDto;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotReadDto;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotUpdateDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.service.CryptoService;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.exception.PasswordException;
import fi.vm.sade.kayttooikeus.service.exception.UsernameAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService;
import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService.LdapSynchronizationType;

@Service
@RequiredArgsConstructor
public class KayttajatiedotServiceImpl implements KayttajatiedotService {

    private final KayttajatiedotRepository kayttajatiedotRepository;
    private final HenkiloDataRepository henkiloDataRepository;
    private final OrikaBeanMapper mapper;
    private final CryptoService cryptoService;
    private final LdapSynchronizationService ldapSynchronizationService;

    @Override
    @Transactional
    public KayttajatiedotReadDto create(String henkiloOid, KayttajatiedotCreateDto dto, LdapSynchronizationType ldapSynchronization) {
        Kayttajatiedot entity = mapper.map(dto, Kayttajatiedot.class);

        Henkilo henkilo = henkiloDataRepository.findByOidHenkilo(henkiloOid)
                .orElseThrow(() -> new NotFoundException("Henkilöä ei löytynyt OID:lla " + henkiloOid));
        if (henkilo.getKayttajatiedot() != null) {
            throw new IllegalArgumentException("Käyttäjätiedot on jo luotu henkilölle");
        }
        kayttajatiedotRepository.findByUsername(entity.getUsername()).ifPresent((Kayttajatiedot t) -> {
            throw new IllegalArgumentException("Käyttäjänimi on jo käytössä");
        });

        entity.setHenkilo(henkilo);
        entity = kayttajatiedotRepository.save(entity);
        henkilo.setKayttajatiedot(entity);

        ldapSynchronization.getAction().accept(ldapSynchronizationService, henkiloOid);
        return mapper.map(entity, KayttajatiedotReadDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public KayttajatiedotReadDto getByHenkiloOid(String henkiloOid) {
        return kayttajatiedotRepository.findByHenkiloOid(henkiloOid)
                .orElseThrow(() -> new NotFoundException("Käyttäjätietoja ei löytynyt OID:lla " + henkiloOid));
    }

    @Override
    @Transactional
    public KayttajatiedotReadDto updateKayttajatiedot(String henkiloOid, KayttajatiedotUpdateDto kayttajatiedotUpdateDto) {

        kayttajatiedotRepository.findByUsername(kayttajatiedotUpdateDto.getUsername()).ifPresent((Kayttajatiedot t) -> {
            throw new IllegalArgumentException("Käyttäjänimi on jo käytössä");
        });

        Henkilo henkilo = henkiloDataRepository.findByOidHenkilo(henkiloOid)
                .orElseThrow(() -> new NotFoundException("Henkilöä ei löytynyt OID:lla " + henkiloOid));

        henkilo.getKayttajatiedot().setUsername(kayttajatiedotUpdateDto.getUsername());
        henkiloDataRepository.save(henkilo);

        this.ldapSynchronizationService.updateHenkiloAsap(henkiloOid);
        return mapper.map(henkilo.getKayttajatiedot(), KayttajatiedotReadDto.class);
    }

    @Override
    @Transactional
    public void changePasswordAsAdmin(String oid, String newPassword) {
        this.cryptoService.throwIfNotStrongPassword(newPassword);
        this.changePassword(oid, newPassword);
    }

    @Override
    @Transactional(readOnly = true)
    public void throwIfUsernameExists(String username) {
        this.kayttajatiedotRepository.findByUsername(username)
                .ifPresent(foundUsername -> {
                    throw new UsernameAlreadyExistsException(String.format("Username %s already exists", foundUsername));
                });
    }

    private void changePassword(String oid, String newPassword) {
        setPasswordForHenkilo(oid, newPassword);
        // Trigger ASAP priority update to LDAP
        ldapSynchronizationService.updateHenkiloAsap(oid);
    }

    private void setPasswordForHenkilo(String oidHenkilo, String password) {
        Kayttajatiedot kayttajatiedot = this.kayttajatiedotRepository.findByHenkiloOidHenkilo(oidHenkilo).orElseGet(() -> {
            Kayttajatiedot newKayttajatiedot = new Kayttajatiedot();
            Henkilo henkilo = this.henkiloDataRepository.findByOidHenkilo(oidHenkilo)
                    .orElseThrow(() -> new NotFoundException("Henkilo not found by oid " + oidHenkilo + " when creating kayttajatiedot"));
            henkilo.setKayttajatiedot(newKayttajatiedot);
            newKayttajatiedot.setHenkilo(henkilo);
            return newKayttajatiedot;
        });
        String salt = this.cryptoService.generateSalt();
        String hash = this.cryptoService.getSaltedHash(password, salt);
        kayttajatiedot.setSalt(salt);
        kayttajatiedot.setPassword(hash);
    }

}
