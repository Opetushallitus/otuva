package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.model.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.repositories.HenkiloRepository;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class HenkiloServiceImpl implements HenkiloService {

    private HenkiloRepository henkiloRepository;

    @Autowired
    HenkiloServiceImpl(HenkiloRepository henkiloRepository) {
        this.henkiloRepository = henkiloRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findHenkilos(HenkiloTyyppi henkiloTyyppi, List<String> ooids, String groupName) {
        List<String> henkiloOids = henkiloRepository.findHenkiloOids(henkiloTyyppi, ooids, groupName);
        //TODO check permissions

        if (CollectionUtils.isEmpty(henkiloOids)) {
            return new ArrayList<>();
        }

        return henkiloOids;
    }
}
