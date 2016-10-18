package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.repositories.DbTestRepository;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HenkiloServiceImpl implements HenkiloService {
    private DbTestRepository dbTestRepository;

    @Autowired
    public HenkiloServiceImpl(DbTestRepository dbTestRepository) {
        this.dbTestRepository = dbTestRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countHenkilos() {
        return dbTestRepository.countHenkilos();
    }
}
