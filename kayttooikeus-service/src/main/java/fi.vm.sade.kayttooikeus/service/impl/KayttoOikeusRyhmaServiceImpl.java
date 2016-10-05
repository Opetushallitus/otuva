package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dao.KayttoOikeusRyhmaDao;
import fi.vm.sade.kayttooikeus.domain.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusRyhmaService;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaDto;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
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
//        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
//        BoundMapperFacade<KayttoOikeusRyhma, KayttoOikeusRyhmaDto> boundMapper =
//                mapperFactory.getMapperFacade(KayttoOikeusRyhma.class, KayttoOikeusRyhmaDto.class);

        return kayttoOikeusRyhmaDao
                .listAll()
                .stream()
//                .map(boundMapper::map)
                .map(kayttoOikeusRyhma -> mapper.map(kayttoOikeusRyhma, KayttoOikeusRyhmaDto.class))
                .collect(Collectors.toList());
    }
}
