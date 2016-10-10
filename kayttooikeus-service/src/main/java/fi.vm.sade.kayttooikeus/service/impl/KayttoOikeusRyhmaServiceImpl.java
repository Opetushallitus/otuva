package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dao.KayttoOikeusRyhmaDao;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusRyhmaService;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by autio on 4.10.2016.
 */
@Service
public class KayttoOikeusRyhmaServiceImpl extends AbstractService implements KayttoOikeusRyhmaService {

    @Autowired
    private KayttoOikeusRyhmaDao kayttoOikeusRyhmaDao;

    @Autowired
    private OrikaBeanMapper mapper;
    
    @Override
    @Transactional(readOnly = true)
    public List<KayttoOikeusRyhmaDto> listAllKayttoOikeusRyhmas() {
        return mapper.mapAsList(kayttoOikeusRyhmaDao.listAll(), KayttoOikeusRyhmaDto.class);
    }
}
