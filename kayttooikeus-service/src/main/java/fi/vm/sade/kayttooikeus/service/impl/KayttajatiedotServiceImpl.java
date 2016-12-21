package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotCreateDto;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotReadDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.repositories.HenkiloRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KayttajatiedotServiceImpl implements KayttajatiedotService {

    private final KayttajatiedotRepository kayttajatiedotRepository;
    private final HenkiloRepository henkiloRepository;
    private final OrikaBeanMapper mapper;

    public KayttajatiedotServiceImpl(KayttajatiedotRepository kayttajatiedotRepository,
            HenkiloRepository henkiloRepository,
            OrikaBeanMapper mapper) {
        this.kayttajatiedotRepository = kayttajatiedotRepository;
        this.henkiloRepository = henkiloRepository;
        this.mapper = mapper;
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

}
