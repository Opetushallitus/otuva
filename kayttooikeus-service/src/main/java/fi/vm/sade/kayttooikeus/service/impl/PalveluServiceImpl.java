package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.repositories.PalveluRepository;
import fi.vm.sade.kayttooikeus.service.PalveluService;
import fi.vm.sade.kayttooikeus.service.dto.PalveluDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        return palveluRepository
                .findAll()
                .stream()
                .map(palvelu -> mapper.map(palvelu, PalveluDto.class))
                .collect(Collectors.toList());
    }
}
