package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.dto.KayttajatiedotReadDto;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class KayttajatiedotServiceImpl implements KayttajatiedotService {

    private final KayttajatiedotRepository kayttajatiedotRepository;

    public KayttajatiedotServiceImpl(KayttajatiedotRepository kayttajatiedotRepository) {
        this.kayttajatiedotRepository = kayttajatiedotRepository;
    }

    @Override
    public KayttajatiedotReadDto getKayttajatiedotByOid(String henkiloOid) {
        return kayttajatiedotRepository.findByHenkiloOid(henkiloOid)
                .orElseThrow(() -> new NotFoundException("Käyttäjätietoja ei löytynyt OID:lla " + henkiloOid));
    }

}
