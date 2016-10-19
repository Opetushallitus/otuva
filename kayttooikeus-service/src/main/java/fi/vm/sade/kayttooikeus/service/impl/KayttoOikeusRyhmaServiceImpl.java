package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaRepository;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepository;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusRyhmaService;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaDto;
import fi.vm.sade.kayttooikeus.service.dto.MyonnettyKayttoOikeusDTO;
import fi.vm.sade.kayttooikeus.util.AccessRightManagementUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class KayttoOikeusRyhmaServiceImpl extends AbstractService implements KayttoOikeusRyhmaService {
    private KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository;
    private OrikaBeanMapper mapper;
    private AccessRightManagementUtils accessRightManagementUtils;
    private MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository;

    @Autowired
    public KayttoOikeusRyhmaServiceImpl(KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository, OrikaBeanMapper mapper,
                                        AccessRightManagementUtils accessRightManagementUtils,
                                        MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository) {
        this.kayttoOikeusRyhmaRepository = kayttoOikeusRyhmaRepository;
        this.mapper = mapper;
        this.accessRightManagementUtils = accessRightManagementUtils;
        this.myonnettyKayttoOikeusRyhmaTapahtumaRepository = myonnettyKayttoOikeusRyhmaTapahtumaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<KayttoOikeusRyhmaDto> listAllKayttoOikeusRyhmas() {
        return mapper.mapAsList(kayttoOikeusRyhmaRepository.listAll(), KayttoOikeusRyhmaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KayttoOikeusRyhmaDto> listPossibleRyhmasByOrganization(String organisaatioOid) {
        List<KayttoOikeusRyhma> allRyhmas = kayttoOikeusRyhmaRepository.listAll();
        accessRightManagementUtils.parseRyhmaLimitationsBasedOnOrgOid(organisaatioOid, allRyhmas);
        return mapper.mapAsList(kayttoOikeusRyhmaRepository.listAll(), KayttoOikeusRyhmaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyonnettyKayttoOikeusDTO> listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos(String henkiloOid, String organisaatioOid, String myontajaOid) {
        List<KayttoOikeusRyhma> allRyhmas;
        /* The list of groups that can be granted must be checked
         * from the granting person's limitation list, if the granting
         * person has any limitations, if not then all groups are listed
         */

        List<Long> slaveIds = accessRightManagementUtils.getGrantableKayttooikeusRyhmas(myontajaOid);

        if (!CollectionUtils.isEmpty(slaveIds)) {
            allRyhmas = kayttoOikeusRyhmaRepository.findByIdList(slaveIds);
        } else {
            allRyhmas = kayttoOikeusRyhmaRepository.listAll();
        }

        /* If groups have limitations based on organization restrictions, those
         * groups must be removed from the list since it confuses the user as UI
         * can't know these limitations and the error message doesn't really help
         */
        accessRightManagementUtils.parseRyhmaLimitationsBasedOnOrgOid(organisaatioOid, allRyhmas);

        if (!allRyhmas.isEmpty()) {
            List<MyonnettyKayttoOikeusRyhmaTapahtuma> henkilosKORs = myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByHenkiloInOrganisaatio(henkiloOid, organisaatioOid);
            return accessRightManagementUtils.createMyonnettyKayttoOikeusDTO(allRyhmas, henkilosKORs);
        }

        return null;
    }
}
