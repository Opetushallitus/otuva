package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.dao.KayttoOikeusRyhmaDao;
import fi.vm.sade.kayttooikeus.domain.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusRyhmaService;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by autio on 4.10.2016.
 */
@Service
public class KayttoOikeusRyhmaServiceImpl implements KayttoOikeusRyhmaService {

    @Autowired
    private KayttoOikeusRyhmaDao kayttoOikeusRyhmaDao;
    
    @Override
    @Transactional(readOnly = true)
    public List<KayttoOikeusRyhmaDto> listAllKayttoOikeusRyhmas() {
        List<KayttoOikeusRyhmaDto> dtos= new ArrayList<KayttoOikeusRyhmaDto>();
        List<KayttoOikeusRyhma> all = kayttoOikeusRyhmaDao.listAll();

        for (KayttoOikeusRyhma kayttoOikeusRyhma : all) {
            KayttoOikeusRyhmaDto dto = new KayttoOikeusRyhmaDto();
            dto.setId(kayttoOikeusRyhma.getId());
            dto.setName(kayttoOikeusRyhma.getName());
            dto.setRooliRajoite(kayttoOikeusRyhma.getRooliRajoite());
            dtos.add(dto);
        }
        return dtos;
    }
}
