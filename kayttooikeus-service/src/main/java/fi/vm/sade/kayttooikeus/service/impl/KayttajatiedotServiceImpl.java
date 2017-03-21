package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotCreateDto;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotReadDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.repositories.HenkiloRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.service.CryptoService;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.exception.PasswordException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class KayttajatiedotServiceImpl implements KayttajatiedotService {

    private final KayttajatiedotRepository kayttajatiedotRepository;
    private final HenkiloRepository henkiloRepository;
    private final OrikaBeanMapper mapper;
    private final CryptoService cryptoService;

    public KayttajatiedotServiceImpl(KayttajatiedotRepository kayttajatiedotRepository,
                                     HenkiloRepository henkiloRepository,
                                     OrikaBeanMapper mapper,
                                     CryptoService cryptoService) {
        this.kayttajatiedotRepository = kayttajatiedotRepository;
        this.henkiloRepository = henkiloRepository;
        this.mapper = mapper;
        this.cryptoService = cryptoService;
    }

    @Override
    @Transactional
    public KayttajatiedotReadDto create(String henkiloOid, KayttajatiedotCreateDto dto) {
        Kayttajatiedot entity = mapper.map(dto, Kayttajatiedot.class);

        Henkilo henkilo = henkiloRepository.findByOidHenkilo(henkiloOid)
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
    public void changePasswordAsAdmin(String oid, String newPassword) {
        this.cryptoService.isStrongPassword(newPassword)
                .stream().findFirst().ifPresent((error) -> {throw new PasswordException(error);});
        this.changePassword(oid, newPassword);
    }

    private void changePassword(String oid, String newPassword) {
        setPasswordForHenkilo(oid, newPassword);
        // Trigger ASAP priority update to LDAP
//        ldapSynchronization.triggerUpdate(oid, null, LdapSynchronization.ASAP_PRIORITY);
    }

    private void setPasswordForHenkilo(String oidHenkilo, String password) {
        Kayttajatiedot kayttajatiedot = this.kayttajatiedotRepository.findByHenkiloOidHenkilo(oidHenkilo).orElseGet(() -> {
            Kayttajatiedot newKayttajatiedot = new Kayttajatiedot();
            Henkilo henkilo = this.henkiloRepository.findByOidHenkilo(oidHenkilo)
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
