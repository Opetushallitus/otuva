package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaRepository;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusRyhmaService;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KayttoOikeusRyhmaServiceImpl extends AbstractService implements KayttoOikeusRyhmaService {
    private KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository;
    private OrikaBeanMapper mapper;

    @Autowired
    public KayttoOikeusRyhmaServiceImpl(KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository, OrikaBeanMapper mapper) {
        this.kayttoOikeusRyhmaRepository = kayttoOikeusRyhmaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<KayttoOikeusRyhmaDto> listAllKayttoOikeusRyhmas() {
        return mapper.mapAsList(kayttoOikeusRyhmaRepository.listAll(), KayttoOikeusRyhmaDto.class);
    }
}
