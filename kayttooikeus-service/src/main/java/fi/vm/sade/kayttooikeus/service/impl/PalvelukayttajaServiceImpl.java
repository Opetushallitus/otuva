package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.dto.KayttajaTyyppi;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaCreateDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaReadDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.service.PalvelukayttajaService;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PalvelukayttajaServiceImpl implements PalvelukayttajaService {

    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;
    private final HenkiloDataRepository henkiloRepository;

    @Override
    public PalvelukayttajaReadDto create(PalvelukayttajaCreateDto createDto) {
        HenkiloCreateDto henkiloCreateDto = new HenkiloCreateDto();
        henkiloCreateDto.setSukunimi(createDto.getNimi());
        // oppijanumerorekisteri pakottaa näiden tietojen syöttämisen
        henkiloCreateDto.setEtunimet("_");
        henkiloCreateDto.setKutsumanimi("_");

        String oid = oppijanumerorekisteriClient.createHenkilo(henkiloCreateDto);

        Henkilo henkilo = henkiloRepository.findByOidHenkilo(oid).orElseGet(Henkilo::new);
        henkilo.setOidHenkilo(oid);
        henkilo.setKayttajaTyyppi(KayttajaTyyppi.PALVELU);
        henkiloRepository.save(henkilo);

        PalvelukayttajaReadDto readDto = new PalvelukayttajaReadDto();
        readDto.setOid(oid);
        readDto.setNimi(createDto.getNimi()); // luotetaan että tämä tallentui
        return readDto;
    }

}
