package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaRepository;
import fi.vm.sade.kayttooikeus.repositories.dto.KayttoOikeusHistoriaDto;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.LocalizationService;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaDto;
import fi.vm.sade.kayttooikeus.repositories.dto.PalveluKayttoOikeusDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KayttoOikeusServiceImpl extends AbstractService implements KayttoOikeusService {
    private KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository;
    private KayttoOikeusRepository kayttoOikeusRepository;
    private LocalizationService localizationService;
    private OrikaBeanMapper mapper;

    @Autowired
    public KayttoOikeusServiceImpl(KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository,
                                   KayttoOikeusRepository kayttoOikeusRepository,
                                   LocalizationService localizationService,
                                   OrikaBeanMapper mapper) {
        this.kayttoOikeusRyhmaRepository = kayttoOikeusRyhmaRepository;
        this.kayttoOikeusRepository = kayttoOikeusRepository;
        this.localizationService = localizationService;
        this.mapper = mapper;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<KayttoOikeusRyhmaDto> listAllKayttoOikeusRyhmas() {
        return mapper.mapAsList(kayttoOikeusRyhmaRepository.listAll(), KayttoOikeusRyhmaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PalveluKayttoOikeusDto> listKayttoOikeusByPalvelu(String palveluName) {
        return localizationService.localize(kayttoOikeusRepository.listKayttoOikeusByPalvelu(palveluName));
    }

    @Override
    @Transactional(readOnly = true)
    public List<KayttoOikeusHistoriaDto> listMyonnettyKayttoOikeusHistoriaForCurrentUser() {
        return localizationService.localize(kayttoOikeusRepository.listMyonnettyKayttoOikeusHistoriaForHenkilo(getCurrentUserOid()));
    }
}
