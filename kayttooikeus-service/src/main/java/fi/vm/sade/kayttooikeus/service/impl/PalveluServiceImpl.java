package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.PalveluDto;
import fi.vm.sade.kayttooikeus.repositories.PalveluRepository;
import fi.vm.sade.kayttooikeus.service.PalveluService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PalveluServiceImpl implements PalveluService {
    private PalveluRepository palveluRepository;
    private OrikaBeanMapper mapper;

    @Autowired
    public PalveluServiceImpl(PalveluRepository palveluRepository, OrikaBeanMapper mapper) {
        this.palveluRepository = palveluRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PalveluDto> listPalvelus() {
        return mapper.mapAsList(palveluRepository.findAll(), PalveluDto.class);
    }
}
