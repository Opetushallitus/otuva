package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.HaettuKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.model.AnomuksenTila;
import fi.vm.sade.kayttooikeus.repositories.HaettuKayttooikeusRyhmaDataRepository;
import fi.vm.sade.kayttooikeus.service.KayttooikeusAnomusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KayttooikeusAnomusServiceImpl implements KayttooikeusAnomusService {

    private final HaettuKayttooikeusRyhmaDataRepository haettuKayttooikeusRyhmaDataRepository;
    private final OrikaBeanMapper mapper;

    @Autowired
    public KayttooikeusAnomusServiceImpl(HaettuKayttooikeusRyhmaDataRepository haettuKayttooikeusRyhmaDataRepository,
                                  OrikaBeanMapper orikaBeanMapper) {
        this.haettuKayttooikeusRyhmaDataRepository = haettuKayttooikeusRyhmaDataRepository;
        this.mapper = orikaBeanMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public List<HaettuKayttooikeusryhmaDto> getAllActiveAnomusByHenkiloOid(String oidHenkilo, boolean activeOnly) {
        if(activeOnly) {
            return mapper.mapAsList(this.haettuKayttooikeusRyhmaDataRepository
                    .findByAnomusHenkiloOidHenkiloAndAnomusAnomuksenTila(oidHenkilo, AnomuksenTila.ANOTTU), HaettuKayttooikeusryhmaDto.class);
        }
        return mapper.mapAsList(this.haettuKayttooikeusRyhmaDataRepository
                .findByAnomusHenkiloOidHenkilo(oidHenkilo), HaettuKayttooikeusryhmaDto.class);
    }
}
