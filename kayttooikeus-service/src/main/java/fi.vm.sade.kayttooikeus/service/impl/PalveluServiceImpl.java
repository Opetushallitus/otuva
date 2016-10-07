package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dao.PalveluDao;
import fi.vm.sade.kayttooikeus.service.PalveluService;
import fi.vm.sade.kayttooikeus.service.dto.PalveluDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by autio on 6.10.2016.
 */
@Service
public class PalveluServiceImpl implements PalveluService {

    @Autowired
    private PalveluDao palveluDao;

    @Autowired
    private OrikaBeanMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<PalveluDto> listPalvelus() {
        return palveluDao
                .findAll()
                .stream()
                .map(palvelu -> mapper.map(palvelu, PalveluDto.class))
                .collect(Collectors.toList());
    }
}
